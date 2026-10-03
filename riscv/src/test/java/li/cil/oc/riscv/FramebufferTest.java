package li.cil.oc.riscv;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Timeout(value = 5, unit = TimeUnit.MINUTES)
public final class FramebufferTest {
    private static final int KEY_Q = 16;
    private static final int KEY_A = 30;
    private static final int KEY_RIGHT = 106;
    private static final int STEPS_PER_KEY_PRESS = 500;
    private static final int STEPS_PER_PIXEL_CHECK = 200;
    private static final int WHITE = 0xFFFFFF;

    @Test
    public void linuxDrawsOnFramebuffer() throws Exception {
        try (final TestMachine test = TestMachine.boot()) {
            test.login();
            // Booting leaves it alone, as the console is not drawn on it.
            assertFalse(test.machine.getFramebuffer().hasChanges());

            test.type("echo $(cat /proc/fb)-$((1+1))");
            test.awaitScreen("0 simple-2");
            // A red pixel at (10, 5), little-endian 0x00RRGGBB.
            test.type("printf '\\0\\0\\377\\0' | dd of=/dev/fb0 bs=4 seek=$((5*320+10)) 2>/dev/null; echo drawn-$((2+1))");
            test.awaitScreen("drawn-3");

            final Framebuffer.Rows rows = test.machine.getFramebuffer().takeChanges();
            assertNotNull(rows);
            assertEquals(5, rows.first());
            assertEquals(1, rows.count());
            assertEquals(0xFF0000, rows.colors()[10]);
            assertEquals(0, rows.colors()[11]);
            assertFalse(test.machine.getFramebuffer().hasChanges());
        }
    }

    @Test
    public void keysReachInputEvents() throws Exception {
        try (final TestMachine test = TestMachine.boot()) {
            test.login();
            // The boot script switched the keyboard of the hidden terminal off, K_OFF.
            test.type("micropython -c \"import ffi; l = ffi.open(None); b = bytearray(4); "
                + "l.func('i', 'ioctl', 'iip')(l.func('i', 'open', 'si')('/dev/tty1', 0o402), 0x4B44, b); "
                + "print('mode-%d' % b[0])\"");
            test.awaitScreen("mode-4");

            // Reads one event, as evdev wants whole ones, then skips its time for its type and code.
            test.type("echo ready-$((1+1)); set -- $(dd if=/dev/input/event0 bs=24 count=1 2>/dev/null | od -An -tu2 -j16 -N4); echo key-$1-$2");
            test.awaitScreen("ready-2");
            // Pressed until it arrives, as od may not have opened the device yet.
            final int[] steps = {0};
            test.awaitCondition("key event", () -> {
                if (steps[0]++ % STEPS_PER_KEY_PRESS == 0) {
                    test.machine.sendKey(KEY_A, true);
                    test.machine.sendKey(KEY_A, false);
                }
                return test.screenText().contains("key-1-30");
            });
        }
    }

    @Test
    public void exampleMovesSquareWithArrowKeys() throws Exception {
        try (final TestMachine test = TestMachine.boot()) {
            test.login();
            test.type("micropython /mnt/builtin/example/framebuffer.py");
            // The square starts in the middle, once the keyboard is open.
            final int x = (Framebuffer.WIDTH - 16) / 2, y = (Framebuffer.HEIGHT - 16) / 2;
            awaitPixel(test, x, y, WHITE);

            test.machine.sendKey(KEY_RIGHT, true);
            test.machine.sendKey(KEY_RIGHT, false);
            awaitPixel(test, x + 16, y, WHITE);
            assertEquals(0x794086, pixel(test, x, y), "Background behind the square.");

            test.machine.sendKey(KEY_Q, true);
            test.machine.sendKey(KEY_Q, false);
            test.awaitScreen("framebuffer demo done");
        }
    }

    private static void awaitPixel(final TestMachine test, final int x, final int y, final int color) throws Exception {
        final int[] steps = {0};
        test.awaitCondition("pixel " + x + "," + y, () ->
            steps[0]++ % STEPS_PER_PIXEL_CHECK == 0 && pixel(test, x, y) == color);
    }

    private static int pixel(final TestMachine test, final int x, final int y) {
        test.machine.getFramebuffer().markChanged();
        final Framebuffer.Rows rows = test.machine.getFramebuffer().takeChanges();
        assertNotNull(rows);
        return rows.colors()[y * Framebuffer.WIDTH + x];
    }
}
