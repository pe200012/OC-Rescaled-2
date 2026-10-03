package li.cil.oc.server.machine.riscv;

import li.cil.oc.api.Driver;
import li.cil.oc.api.driver.DriverItem;
import li.cil.oc.api.driver.item.Memory;
import li.cil.oc.api.driver.item.Processor;
import li.cil.oc.api.internal.TextBuffer;
import li.cil.oc.api.machine.Architecture;
import li.cil.oc.api.machine.ExecutionResult;
import li.cil.oc.api.machine.Machine;
import li.cil.oc.api.machine.Signal;
import li.cil.oc.api.network.EnvironmentHost;
import li.cil.oc.api.network.Network;
import li.cil.oc.api.network.Node;
import li.cil.oc.riscv.MachineSnapshot;
import li.cil.oc.riscv.RiscvMachine;
import li.cil.oc.riscv.bus.DeviceBus;
import li.cil.oc.riscv.inet.InternetLink;
import li.cil.oc.riscv.terminal.KeyboardInput;
import li.cil.oc.riscv.terminal.Terminal;
import li.cil.oc.riscv.terminal.TerminalRenderer;
import li.cil.sedna.api.device.BlockDevice;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Runs a RISC-V Linux machine instead of a Lua state. The console is drawn onto the first screen
 * connected to the machine and fed by its keyboards. Components and signals reach the guest
 * through the device bus, as devices and events.
 * <p>
 * Hard drives become the machine's disks, the first one booted from. A machine with hard drives is
 * saved with the world and resumes where it was when loaded; one without boots from a volatile
 * copy of the bundled system and starts over when loaded.
 */
@Architecture.Name("RISC-V")
public final class RiscvArchitecture implements Architecture {
    private static final Logger LOGGER = LogManager.getLogger("OpenComputers/RISC-V");

    // Memory items report their size in KiB sized for Lua; Linux wants a lot more.
    private static final int MEMORY_SCALE = 16;
    private static final int MAX_MEMORY_SIZE = 256 * 1024 * 1024;
    private static final int[] FREQUENCIES_BY_TIER = {25_000_000, 50_000_000, 100_000_000, 200_000_000};
    private static final int TICKS_PER_SECOND = 20;
    private static final int CYCLES_PER_SLICE = 10_000;
    private static final int CONSOLE_BUFFER_SIZE = 4096;
    private static final String SNAPSHOT_TAG = "oc:riscvSnapshot";

    private final Machine machine;
    private int memorySize;

    private RiscvMachine vm;
    private boolean isPersistent;
    private boolean needsBoot;
    private ComponentDevices devices;
    private DeviceBus bus;
    private NetworkBridge network;
    private List<InternetLink> internetLinks = List.of();
    private int remainingCycles;
    private Terminal terminal;
    private KeyboardInput keyboard;
    private TerminalRenderer renderer;
    private final ByteBuffer console = ByteBuffer.allocate(CONSOLE_BUFFER_SIZE);

    // The screen being drawn to and the keyboards attached to it, the only ones typed with. Picked
    // again on the server thread whenever the machine's components change.
    private TextBuffer screen;
    private Set<String> keyboards = Set.of();
    private boolean needsScreenCheck;

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

    /**
     * Builds the machine on the server thread. Booting waits for the first run, so that a machine
     * being loaded can be restored from its snapshot instead.
     */
    @Override
    public boolean initialize() {
        try {
            final List<BlockDevice> disks = RiscvStorage.openDisks(machine.host());
            isPersistent = !disks.isEmpty();
            // Network cards come first, then internet cards, each with an interface of its own.
            network = new NetworkBridge(machine, NetworkBridge.findCards(machine));
            final List<String> internetCards = NetworkBridge.findComponents(machine, "internet");
            final List<InternetLink> links = new ArrayList<>();
            for (int i = 0; i < internetCards.size(); i++) {
                links.add(new InternetLink(network.networkCount() + i, describeOrigin()));
            }
            internetLinks = links;
            vm = new RiscvMachine(memorySize, isPersistent ? disks : List.of(RiscvMachine.createVolatileRootDisk()),
                network.networkCount() + internetLinks.size());
            vm.setFrequency(frequency());
            devices = new ComponentDevices(machine);
            bus = new DeviceBus(devices, vm.getRpcPort(), vm.getBlobPort(), vm.getEventPort());
            terminal = new Terminal();
            keyboard = new KeyboardInput(terminal);
            renderer = new TerminalRenderer();
            remainingCycles = 0;
            screen = null;
            keyboards = Set.of();
            needsScreenCheck = true;
            needsBoot = true;
            return true;
        } catch (final Exception e) {
            LOGGER.warn("Failed starting RISC-V machine.", e);
            close();
            return false;
        }
    }

    @Override
    public void close() {
        if (vm != null) {
            try {
                vm.close();
            } catch (final Exception e) {
                LOGGER.warn("Failed closing RISC-V machine.", e);
            }
        }
        for (final InternetLink link : internetLinks) {
            link.disconnect();
        }
        internetLinks = List.of();
        vm = null;
        devices = null;
        bus = null;
        network = null;
        terminal = null;
        keyboard = null;
        renderer = null;
        screen = null;
        keyboards = Set.of();
    }

    // --------------------------------------------------------------------- //

    @Override
    public void runSynchronized() {
        bus.runMainThreadCall();
        network.flush();
        if (needsScreenCheck) {
            needsScreenCheck = false;
            selectScreen();
        }
    }

    @Override
    public ExecutionResult runThreaded(final boolean isSynchronizedReturn) {
        try {
            if (needsBoot) {
                vm.boot();
                needsBoot = false;
            }

            if (devices.refresh()) {
                needsScreenCheck = true;
            }
            if (needsScreenCheck) {
                return new ExecutionResult.SynchronizedCall();
            }

            handleSignals();

            // A synchronized call ends a slice early; what is left of the tick's budget carries over.
            if (!isSynchronizedReturn) {
                remainingCycles = vm.getFrequency() / TICKS_PER_SECOND;
            }
            while (remainingCycles > 0 && vm.isRunning()) {
                vm.step(CYCLES_PER_SLICE);
                remainingCycles -= CYCLES_PER_SLICE;
                bus.step();
                network.collect(vm);
                for (final InternetLink link : internetLinks) {
                    link.exchange(vm);
                }
                pumpConsole();
                if (bus.hasMainThreadCall()) {
                    render();
                    return new ExecutionResult.SynchronizedCall();
                }
            }

            render();
            // Frames sent this tick go out together, as cards only send from the server thread.
            if (network.needsServerThread()) {
                return new ExecutionResult.SynchronizedCall();
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

    // --------------------------------------------------------------------- //

    /**
     * Restores the snapshot the machine was saved with. Runs right after {@link #initialize()}.
     */
    @Override
    public void load(final NBTTagCompound nbt) {
        if (vm == null || !isPersistent || !nbt.hasKey(SNAPSHOT_TAG)) {
            return;
        }
        try {
            if (MachineSnapshot.read(RiscvStorage.snapshot(machine.node().address()), nbt.getLong(SNAPSHOT_TAG), vm, bus, terminal)) {
                vm.setFrequency(frequency());
                needsBoot = false;
            }
        } catch (final Exception e) {
            // The restore may have stopped halfway, so start over on fresh devices.
            LOGGER.warn("Failed restoring RISC-V machine, booting it again.", e);
            close();
            initialize();
        }
    }

    /**
     * Writes a snapshot of the running machine. The machine is not executing while this runs.
     */
    @Override
    public void save(final NBTTagCompound nbt) {
        if (vm == null || !isPersistent || needsBoot) {
            return;
        }
        final long id = ThreadLocalRandom.current().nextLong();
        try {
            MachineSnapshot.write(RiscvStorage.snapshot(machine.node().address()), id, vm, bus, terminal);
            nbt.setLong(SNAPSHOT_TAG, id);
        } catch (final Exception e) {
            LOGGER.warn("Failed saving RISC-V machine, it will boot again when loaded.", e);
        }
    }

    // --------------------------------------------------------------------- //

    private String describeOrigin() {
        final EnvironmentHost host = machine.host();
        return String.format(Locale.ROOT, "RISC-V computer at (%d, %d, %d)",
            (int) Math.floor(host.xPosition()), (int) Math.floor(host.yPosition()), (int) Math.floor(host.zPosition()));
    }

    private int frequency() {
        for (final ItemStack stack : machine.host().internalComponents()) {
            final DriverItem driver = stack.isEmpty() ? null : Driver.driverFor(stack);
            if (driver instanceof Processor) {
                return FREQUENCIES_BY_TIER[Math.max(0, Math.min(driver.tier(stack), FREQUENCIES_BY_TIER.length - 1))];
            }
        }
        return FREQUENCIES_BY_TIER[0];
    }

    private void render() {
        if (screen == null) {
            return;
        }
        final TextBuffer target = screen;
        renderer.render(terminal, (column, row, text, foreground, background) -> {
            target.setForegroundColor(foreground);
            target.setBackgroundColor(background);
            target.set(column, row, text, false);
        });
    }

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
            if ("modem_message".equals(signal.name()) && network.accept(vm, args)) {
                continue;
            }
            forwardToBus(signal.name(), args);
            // Machines sharing a component network hear all keyboards; only ours type here.
            if (args.length == 0 || !keyboards.contains(String.valueOf(args[0]))) {
                continue;
            }
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

    // Most signals name the component they come from first; that becomes the event's device.
    private void forwardToBus(final String name, final Object[] args) {
        final UUID device = args.length > 0 ? ComponentDevices.parseAddress(args[0]) : null;
        final Object[] data = device != null ? Arrays.copyOfRange(args, 1, args.length) : args;
        bus.sendEvent(device, name, data);
    }

    /**
     * Picks the screen to draw on: one touching the machine if there is one, as several machines
     * on one component network see each other's screens; otherwise the first by address. Runs on
     * the server thread, as it walks the network and may change the screen's resolution.
     */
    private void selectScreen() {
        final Node own = machine.node();
        final Network network = own != null ? own.network() : null;
        final List<String> screens = NetworkBridge.findComponents(machine, "screen");
        String address = RiscvHooks.adjacentScreen.apply(machine);
        if (address == null || !screens.contains(address)) {
            address = screens.isEmpty() ? null : screens.get(0);
        }

        final Node node = network != null && address != null ? network.node(address) : null;
        final TextBuffer candidate = node != null && node.host() instanceof TextBuffer ? (TextBuffer) node.host() : null;
        keyboards = candidate != null ? Set.copyOf(RiscvHooks.screenKeyboards.apply(machine, address)) : Set.of();
        if (candidate == screen) {
            return;
        }

        screen = null;
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
