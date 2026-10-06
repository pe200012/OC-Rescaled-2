package ocsquared.riscv.terminal;

/**
 * Turns the key codes keyboards report, LWJGL 2's, into Linux input event codes, for the
 * machine's virtio keyboard.
 */
public final class KeyCodes {
    private static final int[] EVDEV = new int[256];

    static {
        // The main block, function keys and keypad share the PC scan codes both are based on.
        for (int code = 0x01; code <= 0x53; code++) {
            EVDEV[code] = code;
        }
        EVDEV[0x57] = 87; // F11
        EVDEV[0x58] = 88; // F12
        for (int code = 0x64; code <= 0x69; code++) {
            EVDEV[code] = 183 + code - 0x64; // F13 to F18
        }
        EVDEV[0x71] = 189; // F19
        EVDEV[0x8D] = 117; // Keypad =
        EVDEV[0x9C] = 96; // Keypad Enter
        EVDEV[0x9D] = 97; // Right Control
        EVDEV[0xB5] = 98; // Keypad /
        EVDEV[0xB8] = 100; // Right Alt
        EVDEV[0xC5] = 119; // Pause
        EVDEV[0xC7] = 102; // Home
        EVDEV[0xC8] = 103; // Up
        EVDEV[0xC9] = 104; // Page Up
        EVDEV[0xCB] = 105; // Left
        EVDEV[0xCD] = 106; // Right
        EVDEV[0xCF] = 107; // End
        EVDEV[0xD0] = 108; // Down
        EVDEV[0xD1] = 109; // Page Down
        EVDEV[0xD2] = 110; // Insert
        EVDEV[0xD3] = 111; // Delete
    }

    private KeyCodes() {
    }

    /**
     * The input event code of a key, or zero for keys the virtio keyboard does not have.
     */
    public static int toEvdev(final int code) {
        return code > 0 && code < EVDEV.length ? EVDEV[code] : 0;
    }
}
