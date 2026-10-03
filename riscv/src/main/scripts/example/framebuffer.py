# framebuffer.py - draws on the framebuffer and moves a square with the arrow keys.
#
# The screen shows the framebuffer as soon as something is drawn on it. Keys are read as input
# events from the machine's keyboard; press q to quit.
#   micropython /mnt/builtin/example/framebuffer.py

import ffi
import struct
import termios
import time

FB = "/sys/class/graphics/fb0/"
SIZE = 16
STEP = 8
EV_KEY = 1
KEY_Q, KEY_UP, KEY_LEFT, KEY_RIGHT, KEY_DOWN = 16, 103, 105, 106, 108
TCIFLUSH = 0


def read_numbers(name):
    with open(FB + name) as f:
        return [int(n) for n in f.read().split(",")]


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


def background(x):
    return [row[x * 4:(x + SIZE) * 4]] * SIZE


fb = open("/dev/fb0", "r+b")
keyboard = open("/dev/input/event0", "rb")
# The keys also go to the terminal: don't echo them, and drop them when done.
saved = termios.tcgetattr(0)
termios.setraw(0)
try:
    for y in range(height):
        fb.seek(y * stride)
        fb.write(row)
    x, y = (width - SIZE) // 2, (height - SIZE) // 2
    draw(fb, x, y, [square] * SIZE)

    while True:
        _, _, kind, code, value = struct.unpack("<qqHHi", keyboard.read(24))
        if kind != EV_KEY or value == 0:
            continue
        if code == KEY_Q:
            break
        dx = {KEY_LEFT: -STEP, KEY_RIGHT: STEP}.get(code, 0)
        dy = {KEY_UP: -STEP, KEY_DOWN: STEP}.get(code, 0)
        nx = min(max(x + dx, 0), width - SIZE)
        ny = min(max(y + dy, 0), height - SIZE)
        if (nx, ny) != (x, y):
            draw(fb, x, y, background(x))
            x, y = nx, ny
            draw(fb, x, y, [square] * SIZE)
finally:
    termios.tcsetattr(0, termios.TCSANOW, saved)
    ffi.open(None).func("i", "tcflush", "ii")(0, TCIFLUSH)

# The console is shown again when it prints after the drawing stopped for a second.
time.sleep(1)
print("framebuffer demo done")
