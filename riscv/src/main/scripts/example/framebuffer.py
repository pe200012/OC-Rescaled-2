# framebuffer.py - draws on the framebuffer and moves a square with the arrow keys, or to where
# the screen is clicked or dragged.
#
# The screen shows the framebuffer as soon as something is drawn on it. Keys and clicks are read
# as input events from the machine's keyboard and tablet; press q to quit.
#   micropython /mnt/builtin/example/framebuffer.py

import ffi
import os
import select
import struct
import termios
import time

FB = "/sys/class/graphics/fb0/"
INPUT = "/sys/class/input/"
SIZE = 16
STEP = 8
EV_SYN, EV_KEY, EV_ABS = 0, 1, 3
KEY_Q, KEY_UP, KEY_LEFT, KEY_RIGHT, KEY_DOWN = 16, 103, 105, 106, 108
BTN_LEFT = 0x110
TCIFLUSH = 0


def read_numbers(name):
    with open(FB + name) as f:
        return [int(n) for n in f.read().split(",")]


def open_input(name):
    for entry in os.listdir(INPUT):
        if entry.startswith("event"):
            with open(INPUT + entry + "/device/name") as f:
                if f.read().strip() == name:
                    return open("/dev/input/" + entry, "rb")
    raise OSError("no input device " + name)


width, height = read_numbers("virtual_size")
stride = read_numbers("stride")[0]

# Pixels are little-endian 0x00RRGGBB: blue, green, red, unused. Red to blue, left to right.
row = bytearray(width * 4)
for x in range(width):
    red = 255 * x // (width - 1)
    row[x * 4:x * 4 + 3] = bytes((255 - red, 64, red))
square = b"\xff\xff\xff\x00" * SIZE


def draw(fb, x, y, pixels):
    for i in range(SIZE):
        fb.seek((y + i) * stride + x * 4)
        fb.write(pixels[i])


def move(fb, x, y, nx, ny):
    nx = min(max(nx, 0), width - SIZE)
    ny = min(max(ny, 0), height - SIZE)
    if (nx, ny) != (x, y):
        draw(fb, x, y, [row[x * 4:(x + SIZE) * 4]] * SIZE)
        draw(fb, nx, ny, [square] * SIZE)
    return nx, ny


fb = open("/dev/fb0", "r+b")
keyboard = open_input("virtio_keyboard")
tablet = open_input("virtio_tablet")
poller = select.poll()
poller.register(keyboard, select.POLLIN)
poller.register(tablet, select.POLLIN)
# The keys also go to the terminal: don't echo them, and drop them when done.
saved = termios.tcgetattr(0)
termios.setraw(0)
try:
    for y in range(height):
        fb.seek(y * stride)
        fb.write(row)
    x, y = (width - SIZE) // 2, (height - SIZE) // 2
    draw(fb, x, y, [square] * SIZE)

    pointer = [0, 0]
    pressed = False
    running = True
    while running:
        for device, _ in poller.poll():
            _, _, kind, code, value = struct.unpack("<qqHHi", device.read(24))
            if device is tablet:
                # Positions are in pixels; ABS_X is code 0, ABS_Y code 1.
                if kind == EV_ABS:
                    pointer[code] = value
                elif kind == EV_KEY and code == BTN_LEFT:
                    pressed = value == 1
                elif kind == EV_SYN and pressed:
                    x, y = move(fb, x, y, pointer[0] - SIZE // 2, pointer[1] - SIZE // 2)
            elif kind == EV_KEY and value != 0:
                if code == KEY_Q:
                    running = False
                    break
                dx = {KEY_LEFT: -STEP, KEY_RIGHT: STEP}.get(code, 0)
                dy = {KEY_UP: -STEP, KEY_DOWN: STEP}.get(code, 0)
                x, y = move(fb, x, y, x + dx, y + dy)
finally:
    termios.tcsetattr(0, termios.TCSANOW, saved)
    ffi.open(None).func("i", "tcflush", "ii")(0, TCIFLUSH)

# The console is shown again when it prints after the drawing stopped for a second.
time.sleep(1)
print("framebuffer demo done")
