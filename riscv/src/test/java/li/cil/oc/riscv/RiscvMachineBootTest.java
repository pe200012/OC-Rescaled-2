package li.cil.oc.riscv;

import li.cil.oc.riscv.terminal.KeyboardInput;
import li.cil.oc.riscv.terminal.Terminal;
import li.cil.oc.riscv.terminal.TerminalRenderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Timeout(value = 5, unit = TimeUnit.MINUTES)
public final class RiscvMachineBootTest {
    private static final int MEMORY_SIZE = 32 * 1024 * 1024;
    private static final long MAX_CYCLES = 2_000_000_000L;
    private static final int CYCLES_PER_STEP = 10_000;
    private static final int KEY_RETURN = 28;

    private final Terminal terminal = new Terminal();
    private final KeyboardInput keyboard = new KeyboardInput(terminal);
    private final TerminalRenderer renderer = new TerminalRenderer();
    private final char[][] screen = new char[Terminal.HEIGHT][Terminal.WIDTH];
    private RiscvMachine machine;
    private long cycles;

    @Test
    public void bootsToShellOnTerminalAndPowersOff() throws Exception {
        for (final char[] row : screen) {
            Arrays.fill(row, ' ');
        }
        machine = new RiscvMachine(MEMORY_SIZE, RiscvMachine.createDefaultRootDisk());
        machine.boot();

        awaitScreen("login:");
        type("root");
        awaitScreen("# ");
        type("echo hi-$((6*7))");
        awaitScreen("hi-42");
        type("poweroff");

        final long deadline = cycles + MAX_CYCLES;
        while (machine.isRunning()) {
            assertTrue(cycles < deadline, () -> "Timed out waiting for poweroff. Screen:\n" + screenText());
            step();
        }
        assertFalse(machine.isRunning());
    }

    // --------------------------------------------------------------------- //

    private void type(final String line) {
        for (final char ch : line.toCharArray()) {
            keyboard.keyDown(ch, 0);
        }
        keyboard.keyDown('\r', KEY_RETURN);
    }

    private void awaitScreen(final String expected) throws Exception {
        final long deadline = cycles + MAX_CYCLES;
        while (!screenText().contains(expected)) {
            assertTrue(machine.isRunning(), () -> "Machine stopped waiting for [" + expected + "]. Screen:\n" + screenText());
            assertTrue(cycles < deadline, () -> "Timed out waiting for [" + expected + "]. Screen:\n" + screenText());
            step();
        }
    }

    private void step() throws Exception {
        machine.step(CYCLES_PER_STEP);
        cycles += CYCLES_PER_STEP;

        final ByteBuffer output = ByteBuffer.allocate(4096);
        int value;
        while (output.hasRemaining() && (value = machine.readConsole()) >= 0) {
            output.put((byte) value);
        }
        output.flip();
        terminal.putOutput(output);

        while (machine.canWriteConsole() && (value = terminal.readInput()) >= 0) {
            machine.writeConsole((byte) value);
        }

        renderer.render(terminal, (column, row, text, foreground, background) ->
            text.getChars(0, text.length(), screen[row], column));
    }

    private String screenText() {
        final StringBuilder builder = new StringBuilder();
        for (final char[] row : screen) {
            builder.append(row).append('\n');
        }
        return builder.toString();
    }
}
