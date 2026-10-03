package li.cil.oc.riscv;

import li.cil.oc.riscv.bus.DeviceBus;
import li.cil.oc.riscv.terminal.KeyboardInput;
import li.cil.oc.riscv.terminal.Terminal;
import li.cil.oc.riscv.terminal.TerminalRenderer;

import javax.annotation.Nullable;
import java.nio.ByteBuffer;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Drives a machine the way the architecture does, with the console rendered onto a character grid.
 */
final class TestMachine {
    private static final int MEMORY_SIZE = 32 * 1024 * 1024;
    private static final long MAX_CYCLES = 2_000_000_000L;
    private static final int CYCLES_PER_STEP = 10_000;
    private static final int KEY_RETURN = 28;

    final RiscvMachine machine;
    @Nullable final DeviceBus bus;
    private final Terminal terminal = new Terminal();
    private final KeyboardInput keyboard = new KeyboardInput(terminal);
    private final TerminalRenderer renderer = new TerminalRenderer();
    private final char[][] screen = new char[Terminal.HEIGHT][Terminal.WIDTH];
    private long cycles;

    TestMachine(@Nullable final DeviceBus.Devices devices) throws Exception {
        for (final char[] row : screen) {
            Arrays.fill(row, ' ');
        }
        machine = new RiscvMachine(MEMORY_SIZE, RiscvMachine.createDefaultRootDisk());
        bus = devices != null
            ? new DeviceBus(devices, machine.getRpcPort(), machine.getBlobPort(), machine.getEventPort())
            : null;
        machine.boot();
    }

    void login() throws Exception {
        awaitScreen("login:");
        type("root");
        awaitScreen("# ");
    }

    void type(final String line) {
        for (final char ch : line.toCharArray()) {
            keyboard.keyDown(ch, 0);
        }
        keyboard.keyDown('\r', KEY_RETURN);
    }

    void awaitScreen(final String expected) throws Exception {
        final long deadline = cycles + MAX_CYCLES;
        while (!screenText().contains(expected)) {
            assertTrue(machine.isRunning(), () -> "Machine stopped waiting for [" + expected + "]. Screen:\n" + screenText());
            assertTrue(cycles < deadline, () -> "Timed out waiting for [" + expected + "]. Screen:\n" + screenText());
            step();
        }
    }

    void awaitPowerOff() throws Exception {
        final long deadline = cycles + MAX_CYCLES;
        while (machine.isRunning()) {
            assertTrue(cycles < deadline, () -> "Timed out waiting for poweroff. Screen:\n" + screenText());
            step();
        }
    }

    String screenText() {
        final StringBuilder builder = new StringBuilder();
        for (final char[] row : screen) {
            builder.append(row).append('\n');
        }
        return builder.toString();
    }

    private void step() throws Exception {
        machine.step(CYCLES_PER_STEP);
        cycles += CYCLES_PER_STEP;

        if (bus != null) {
            bus.step();
            // Stands in for the server thread picking up synchronized calls.
            if (bus.hasMainThreadCall()) {
                bus.runMainThreadCall();
            }
        }

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
}
