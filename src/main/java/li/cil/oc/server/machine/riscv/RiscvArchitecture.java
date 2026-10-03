package li.cil.oc.server.machine.riscv;

import li.cil.oc.api.Driver;
import li.cil.oc.api.driver.DriverItem;
import li.cil.oc.api.driver.item.Memory;
import li.cil.oc.api.internal.TextBuffer;
import li.cil.oc.api.machine.Architecture;
import li.cil.oc.api.machine.ExecutionResult;
import li.cil.oc.api.machine.Machine;
import li.cil.oc.api.machine.Signal;
import li.cil.oc.api.network.Network;
import li.cil.oc.api.network.Node;
import li.cil.oc.riscv.RiscvMachine;
import li.cil.oc.riscv.terminal.KeyboardInput;
import li.cil.oc.riscv.terminal.Terminal;
import li.cil.oc.riscv.terminal.TerminalRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Map;

/**
 * Runs a RISC-V Linux machine instead of a Lua state. The console is drawn onto the first screen
 * connected to the machine and fed by its keyboards.
 * <p>
 * Not persisted yet: a machine that gets loaded while running boots again from scratch.
 */
@Architecture.Name("RISC-V")
public final class RiscvArchitecture implements Architecture {
    private static final Logger LOGGER = LogManager.getLogger("OpenComputers/RISC-V");

    // Memory items report their size in KiB sized for Lua; Linux wants a lot more.
    private static final int MEMORY_SCALE = 16;
    private static final int MAX_MEMORY_SIZE = 256 * 1024 * 1024;
    private static final int FREQUENCY = 25_000_000;
    private static final int TICKS_PER_SECOND = 20;
    private static final int CYCLES_PER_SLICE = 10_000;
    private static final int CONSOLE_BUFFER_SIZE = 4096;

    private final Machine machine;
    private int memorySize;

    private RiscvMachine vm;
    private Terminal terminal;
    private KeyboardInput keyboard;
    private TerminalRenderer renderer;
    private final ByteBuffer console = ByteBuffer.allocate(CONSOLE_BUFFER_SIZE);

    // The screen being drawn to, and one found but waiting for setup on the server thread.
    private TextBuffer screen;
    private TextBuffer pendingScreen;

    public RiscvArchitecture(final Machine machine) {
        this.machine = machine;
    }

    // --------------------------------------------------------------------- //

    @Override
    public boolean isInitialized() {
        return vm != null;
    }

    @Override
    public boolean recomputeMemory(final Iterable<ItemStack> components) {
        double kibibytes = 0;
        for (final ItemStack stack : components) {
            final DriverItem driver = Driver.driverFor(stack);
            if (driver instanceof Memory) {
                kibibytes += ((Memory) driver).amount(stack);
            }
        }
        memorySize = (int) Math.min(kibibytes * 1024 * MEMORY_SCALE, MAX_MEMORY_SIZE);
        return memorySize > 0;
    }

    @Override
    public boolean initialize() {
        try {
            vm = new RiscvMachine(memorySize, RiscvMachine.createDefaultRootDisk());
            vm.setFrequency(FREQUENCY);
            terminal = new Terminal();
            keyboard = new KeyboardInput(terminal);
            renderer = new TerminalRenderer();
            screen = null;
            pendingScreen = null;
            vm.boot();
            return true;
        } catch (final Exception e) {
            LOGGER.warn("Failed starting RISC-V machine.", e);
            close();
            return false;
        }
    }

    @Override
    public void close() {
        vm = null;
        terminal = null;
        keyboard = null;
        renderer = null;
        screen = null;
        pendingScreen = null;
    }

    // --------------------------------------------------------------------- //

    @Override
    public void runSynchronized() {
        // Changing the resolution is not safe from the executor thread.
        final TextBuffer candidate = pendingScreen;
        pendingScreen = null;
        if (candidate == null) {
            return;
        }
        if (candidate.getMaximumWidth() < Terminal.WIDTH || candidate.getMaximumHeight() < Terminal.HEIGHT) {
            machine.crash("RISC-V needs a screen of at least " + Terminal.WIDTH + "x" + Terminal.HEIGHT + ".");
            return;
        }
        candidate.setResolution(Terminal.WIDTH, Terminal.HEIGHT);
        renderer.invalidate();
        screen = candidate;
    }

    @Override
    public ExecutionResult runThreaded(final boolean isSynchronizedReturn) {
        try {
            final TextBuffer found = findScreen();
            if (found == null) {
                screen = null;
            } else if (found != screen) {
                pendingScreen = found;
                return new ExecutionResult.SynchronizedCall();
            }

            handleSignals();

            int remaining = vm.getFrequency() / TICKS_PER_SECOND;
            while (remaining > 0 && vm.isRunning()) {
                vm.step(CYCLES_PER_SLICE);
                remaining -= CYCLES_PER_SLICE;
                pumpConsole();
            }

            if (screen != null) {
                final TextBuffer target = screen;
                renderer.render(terminal, (column, row, text, foreground, background) -> {
                    target.setForegroundColor(foreground);
                    target.setBackgroundColor(background);
                    target.set(column, row, text, false);
                });
            }

            return vm.isRunning() ? new ExecutionResult.Sleep(1) : new ExecutionResult.Shutdown(false);
        } catch (final Throwable e) {
            LOGGER.warn("RISC-V machine failed.", e);
            return new ExecutionResult.Error(String.valueOf(e.getMessage()));
        }
    }

    @Override
    public void onSignal() {
    }

    @Override
    public void onConnect() {
    }

    @Override
    public void load(final NBTTagCompound nbt) {
    }

    @Override
    public void save(final NBTTagCompound nbt) {
    }

    // --------------------------------------------------------------------- //

    private void pumpConsole() {
        console.clear();
        int value;
        while (console.hasRemaining() && (value = vm.readConsole()) >= 0) {
            console.put((byte) value);
        }
        console.flip();
        if (console.hasRemaining()) {
            terminal.putOutput(console);
        }

        while (vm.canWriteConsole() && (value = terminal.readInput()) >= 0) {
            vm.writeConsole((byte) value);
        }
    }

    private void handleSignals() {
        Signal signal;
        while ((signal = machine.popSignal()) != null) {
            final Object[] args = signal.args();
            switch (signal.name()) {
                case "key_down" -> {
                    if (args.length >= 3) {
                        keyboard.keyDown(toInt(args[1]), toInt(args[2]));
                    }
                }
                case "key_up" -> {
                    if (args.length >= 3) {
                        keyboard.keyUp(toInt(args[2]));
                    }
                }
                case "clipboard" -> {
                    if (args.length >= 2) {
                        keyboard.paste(toText(args[1]));
                    }
                }
                default -> {
                }
            }
        }
    }

    private TextBuffer findScreen() {
        final Node node = machine.node();
        final Network network = node != null ? node.network() : null;
        if (network == null) {
            return null;
        }
        // The machine updates this map from the server thread without a lock we can take here.
        final Map<String, String> components;
        try {
            components = new HashMap<>(machine.components());
        } catch (final ConcurrentModificationException e) {
            return screen;
        }
        for (final Map.Entry<String, String> component : components.entrySet()) {
            if (!"screen".equals(component.getValue())) {
                continue;
            }
            final Node screenNode = network.node(component.getKey());
            if (screenNode != null && screenNode.host() instanceof TextBuffer) {
                return (TextBuffer) screenNode.host();
            }
        }
        return null;
    }

    private static int toInt(final Object value) {
        return value instanceof Number ? ((Number) value).intValue() : 0;
    }

    private static String toText(final Object value) {
        if (value instanceof byte[]) {
            return new String((byte[]) value, StandardCharsets.UTF_8);
        }
        return String.valueOf(value);
    }
}
