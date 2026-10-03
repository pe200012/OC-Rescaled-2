package li.cil.oc.riscv;

import li.cil.oc.riscv.bus.DeviceBus;
import li.cil.oc.riscv.terminal.KeyboardInput;
import li.cil.oc.riscv.terminal.Terminal;
import li.cil.oc.riscv.terminal.TerminalRenderer;
import li.cil.sedna.api.device.BlockDevice;

import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.function.BooleanSupplier;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Drives a machine the way the architecture does, with the console rendered onto a character grid.
 */
final class TestMachine implements AutoCloseable {
    private static final int MEMORY_SIZE = 32 * 1024 * 1024;
    private static final int BARE_METAL_MEMORY_SIZE = 1024 * 1024;
    private static final long MAX_CYCLES = 2_000_000_000L;
    private static final int CYCLES_PER_STEP = 10_000;
    private static final int KEY_RETURN = 28;

    static final DeviceBus.Devices NO_DEVICES = new DeviceBus.Devices() {
        @Override
        public int generation() {
            return 0;
        }

        @Override
        public List<DeviceBus.DeviceInfo> list() {
            return List.of();
        }

        @Override
        public List<DeviceBus.MethodInfo> methods(final java.util.UUID device) {
            return null;
        }

        @Override
        public Object[] invoke(final java.util.UUID device, final String method, final Object[] arguments, final boolean isMainThread) throws Exception {
            throw new NoSuchMethodException();
        }
    };

    final RiscvMachine machine;
    final DeviceBus bus;
    private final Terminal terminal = new Terminal();
    private final KeyboardInput keyboard = new KeyboardInput(terminal);
    private final TerminalRenderer renderer = new TerminalRenderer();
    private final char[][] screen = new char[Terminal.HEIGHT][Terminal.WIDTH];
    private long cycles;

    // Runs after every step, e.g. to step a second machine and carry frames between them.
    Runnable afterStep = () -> {
    };

    private TestMachine(final DeviceBus.Devices devices, final int memorySize, final byte[] firmware,
                        final List<BlockDevice> disks, final int networkCount) throws Exception {
        for (final char[] row : screen) {
            Arrays.fill(row, ' ');
        }
        machine = new RiscvMachine(memorySize, firmware, disks, networkCount);
        bus = new DeviceBus(devices, machine.getRpcPort(), machine.getBlobPort(), machine.getEventPort());
        machine.getWindow().setDevices(devices);
    }

    private TestMachine(final DeviceBus.Devices devices, final List<BlockDevice> disks, final int networkCount) throws Exception {
        this(devices, MEMORY_SIZE, RiscvMachine.linuxBootloader(), disks, networkCount);
    }

    private TestMachine(final DeviceBus.Devices devices, final List<BlockDevice> disks) throws Exception {
        this(devices, disks, 0);
    }

    /**
     * Boots a program on a small machine without disks, like a microcontroller.
     */
    static TestMachine bareMetal(final byte[] firmware, final DeviceBus.Devices devices) throws Exception {
        final TestMachine test = new TestMachine(devices, BARE_METAL_MEMORY_SIZE, firmware, List.of(), 0);
        test.machine.boot();
        return test;
    }

    static TestMachine bootWithNetwork() throws Exception {
        final TestMachine test = new TestMachine(NO_DEVICES, List.of(RiscvMachine.createVolatileRootDisk()), 1);
        test.machine.boot();
        return test;
    }

    static TestMachine boot() throws Exception {
        return boot(NO_DEVICES, List.of(RiscvMachine.createVolatileRootDisk()));
    }

    static TestMachine boot(final List<BlockDevice> disks) throws Exception {
        return boot(NO_DEVICES, disks);
    }

    static TestMachine boot(final DeviceBus.Devices devices) throws Exception {
        return boot(devices, List.of(RiscvMachine.createVolatileRootDisk()));
    }

    static TestMachine boot(final DeviceBus.Devices devices, final List<BlockDevice> disks) throws Exception {
        final TestMachine test = new TestMachine(devices, disks);
        test.machine.boot();
        return test;
    }

    static TestMachine restore(final Path snapshot, final long id, final List<BlockDevice> disks) throws Exception {
        final TestMachine test = new TestMachine(NO_DEVICES, disks);
        assertTrue(MachineSnapshot.read(snapshot, id, test.machine, test.bus, test.terminal), "No snapshot to restore.");
        return test;
    }

    void snapshot(final Path file, final long id) throws Exception {
        MachineSnapshot.write(file, id, machine, bus, terminal);
    }

    @Override
    public void close() throws Exception {
        machine.close();
    }

    // --------------------------------------------------------------------- //

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

    /**
     * Runs until the condition holds or the machine stops.
     */
    void awaitCondition(final String description, final BooleanSupplier condition) throws Exception {
        final long deadline = cycles + MAX_CYCLES;
        while (!condition.getAsBoolean()) {
            assertTrue(machine.isRunning(), () -> "Machine stopped waiting for " + description + ".");
            assertTrue(cycles < deadline, () -> "Timed out waiting for " + description + ".");
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
        stepAlone();
        afterStep.run();
    }

    void stepAlone() throws Exception {
        machine.step(CYCLES_PER_STEP);
        cycles += CYCLES_PER_STEP;

        bus.step();
        // Stands in for the server thread picking up synchronized calls.
        if (bus.hasMainThreadCall()) {
            bus.runMainThreadCall();
        }
        if (machine.getWindow().hasMainThreadCall()) {
            machine.getWindow().runMainThreadCall();
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
