package li.cil.oc.riscv;

import li.cil.ceres.BinarySerialization;
import li.cil.oc.riscv.bus.ComponentWindow;
import li.cil.sedna.Sedna;
import li.cil.sedna.api.Sizes;
import li.cil.sedna.api.device.BlockDevice;
import li.cil.sedna.api.device.Device;
import li.cil.sedna.api.device.PhysicalMemory;
import li.cil.sedna.api.device.serial.SerialDevice;
import li.cil.sedna.api.devicetree.DevicePropertyNames;
import li.cil.sedna.api.devicetree.DeviceTree;
import li.cil.sedna.api.devicetree.DeviceTreeProvider;
import li.cil.sedna.api.memory.MemoryAccessException;
import li.cil.sedna.api.memory.MemoryMap;
import li.cil.sedna.buildroot.Buildroot;
import li.cil.sedna.device.block.ByteBufferBlockDevice;
import li.cil.sedna.device.memory.Memory;
import li.cil.sedna.device.rtc.GoldfishRTC;
import li.cil.sedna.device.rtc.SystemTimeRealTimeCounter;
import li.cil.sedna.device.serial.UART16550A;
import li.cil.sedna.device.virtio.VirtIOBlockDevice;
import li.cil.sedna.device.virtio.VirtIOConsoleDevice;
import li.cil.sedna.device.virtio.VirtIOFileSystemDevice;
import li.cil.sedna.device.virtio.VirtIONetworkDevice;
import li.cil.sedna.devicetree.DeviceTreeRegistry;
import li.cil.sedna.fs.FileSystem;
import li.cil.sedna.fs.ZipStreamFileSystem;
import li.cil.sedna.riscv.R5Board;

import javax.annotation.Nullable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * A RISC-V board: RAM, a UART console, a real time clock, disks, network interfaces, the device bus
 * ports, the component window and the guest scripts. Boots the firmware it is given, copied to the
 * start of RAM: the bundled Linux boot loader, or a bare-metal program. Independent of Minecraft,
 * so it can be booted from tests.
 */
public final class RiscvMachine implements AutoCloseable {
    private static final long MEMORY_ADDRESS = 0x80000000L;
    private static final long FIRMWARE_ALIGNMENT = 0x1000;
    // Linux maps the component window as a UIO device, for programs to use without root's help.
    private static final String WINDOW_COMPATIBLE = "oc,component-bus";
    private static final String BOOT_ARGUMENTS = "root=/dev/vda rw uio_pdrv_genirq.of_id=" + WINDOW_COMPATIBLE;

    // Disks sit at fixed addresses in slot order, so the first one is always /dev/vda.
    private static final long DISK_BASE_ADDRESS = 0x20000000L;
    private static final int DISK_STRIDE = 0x1000;
    private static final int[] DISK_INTERRUPTS = {0x1, 0x6, 0x7, 0x8, 0x9, 0xC};
    // Network interfaces likewise, so the first one is always eth0.
    private static final long NETWORK_BASE_ADDRESS = 0x21000000L;
    private static final int NETWORK_STRIDE = 0x1000;
    private static final int[] NETWORK_INTERRUPTS = {0xD, 0xE, 0xF, 0x10, 0x11, 0x12};
    // Bare-metal programs find the component window here.
    private static final long WINDOW_ADDRESS = 0x30000000L;
    private static final int BUS_INTERRUPT = 0x3;
    private static final int WINDOW_INTERRUPT = 0x4;
    private static final int SCRIPTS_INTERRUPT = 0x5;
    private static final int UART_INTERRUPT = 0xA;
    private static final int RTC_INTERRUPT = 0xB;

    // Names the guest's device bus libraries look for, shared with OpenComputers II.
    private static final String[] BUS_PORT_NAMES = {"oc2.rpc.0", "oc2.blob.0", "oc2.event.0"};
    private static final String SCRIPTS_TAG = "builtin";
    private static final String SCRIPTS_RESOURCE = "/li/cil/oc/riscv/scripts.zip";

    private static final int STATE_VERSION = 3;
    private static final int MEMORY_COPY_CHUNK = 64 * 1024;

    private static byte[] linuxBootloader;
    private static FileSystem scripts;

    static {
        DeviceTreeRegistry.putProvider(ComponentWindow.class, new DeviceTreeProvider() {
            @Override
            public Optional<String> getName(final Device device) {
                return Optional.of("component-bus");
            }

            @Override
            public void visit(final DeviceTree node, final MemoryMap memoryMap, final Device device) {
                node.addProp(DevicePropertyNames.COMPATIBLE, WINDOW_COMPATIBLE);
            }
        });
    }

    private final byte[] firmware;
    private final R5Board board = new R5Board();
    private final PhysicalMemory memory;
    private final UART16550A uart = new UART16550A();
    private final GoldfishRTC rtc = new GoldfishRTC(SystemTimeRealTimeCounter.get());
    private final List<BlockDevice> disks;
    private final VirtIOBlockDevice[] diskDevices;
    private final VirtIONetworkDevice[] networkDevices;
    private final VirtIOConsoleDevice busPorts;
    private final VirtIOFileSystemDevice builtin;
    private final ComponentWindow window;

    /**
     * @param firmware     the program to run, such as {@link #linuxBootloader()}.
     * @param disks        the disks in slot order; the first one is booted from. Closed with the machine.
     * @param networkCount how many network interfaces the machine has.
     */
    public RiscvMachine(final int memorySize, final byte[] firmware, final List<BlockDevice> disks, final int networkCount) throws IOException {
        if (firmware.length == 0 || firmware.length > memorySize / 2) {
            throw new IllegalArgumentException("Firmware does not fit into memory.");
        }
        if (disks.size() > DISK_INTERRUPTS.length) {
            throw new IllegalArgumentException("At most " + DISK_INTERRUPTS.length + " disks are supported.");
        }
        if (networkCount > NETWORK_INTERRUPTS.length) {
            throw new IllegalArgumentException("At most " + NETWORK_INTERRUPTS.length + " network interfaces are supported.");
        }
        Sedna.initialize();

        this.firmware = firmware.clone();
        memory = Memory.create(memorySize);
        this.disks = List.copyOf(disks);
        diskDevices = new VirtIOBlockDevice[disks.size()];
        networkDevices = new VirtIONetworkDevice[networkCount];
        busPorts = new VirtIOConsoleDevice(board.getMemoryMap(), BUS_PORT_NAMES);
        builtin = new VirtIOFileSystemDevice(board.getMemoryMap(), SCRIPTS_TAG, getScripts());
        window = new ComponentWindow(() -> board.getCpu().getFrequency());

        uart.getInterrupt().set(UART_INTERRUPT, board.getInterruptController());
        rtc.getInterrupt().set(RTC_INTERRUPT, board.getInterruptController());
        busPorts.getInterrupt().set(BUS_INTERRUPT, board.getInterruptController());
        builtin.getInterrupt().set(SCRIPTS_INTERRUPT, board.getInterruptController());
        window.getInterrupt().set(WINDOW_INTERRUPT, board.getInterruptController());

        if (!board.addDevice(MEMORY_ADDRESS, memory)) {
            throw new IllegalStateException("Failed mapping memory.");
        }
        for (int i = 0; i < diskDevices.length; i++) {
            diskDevices[i] = new VirtIOBlockDevice(board.getMemoryMap(), disks.get(i));
            diskDevices[i].getInterrupt().set(DISK_INTERRUPTS[i], board.getInterruptController());
            if (!board.addDevice(DISK_BASE_ADDRESS + (long) i * DISK_STRIDE, diskDevices[i])) {
                throw new IllegalStateException("Failed mapping disk " + i + ".");
            }
        }
        for (int i = 0; i < networkDevices.length; i++) {
            networkDevices[i] = new VirtIONetworkDevice(board.getMemoryMap());
            networkDevices[i].getInterrupt().set(NETWORK_INTERRUPTS[i], board.getInterruptController());
            if (!board.addDevice(NETWORK_BASE_ADDRESS + (long) i * NETWORK_STRIDE, networkDevices[i])) {
                throw new IllegalStateException("Failed mapping network interface " + i + ".");
            }
        }
        if (!board.addDevice(WINDOW_ADDRESS, window)) {
            throw new IllegalStateException("Failed mapping component window.");
        }
        if (board.addDevice(uart).isEmpty() || board.addDevice(rtc).isEmpty()
            || board.addDevice(busPorts).isEmpty() || board.addDevice(builtin).isEmpty()) {
            throw new IllegalStateException("Failed mapping devices.");
        }

        board.setBootArguments(BOOT_ARGUMENTS);
        board.setStandardOutputDevice(uart);
        final long firmwareRegion = (firmware.length + FIRMWARE_ALIGNMENT - 1) & -FIRMWARE_ALIGNMENT;
        board.setFirmwareSize(Math.max(firmwareRegion, Buildroot.getSednaFirmwareRegionSize()));
    }

    /**
     * The bundled Linux boot loader. It boots the kernel on the first disk.
     */
    public static synchronized byte[] linuxBootloader() throws IOException {
        if (linuxBootloader == null) {
            try (final InputStream stream = Buildroot.getSednaFirmware()) {
                linuxBootloader = stream.readAllBytes();
            }
        }
        return linuxBootloader.clone();
    }

    public static boolean isLinuxBootloader(final byte[] firmware) throws IOException {
        return Arrays.equals(firmware, linuxBootloader());
    }

    /**
     * A fresh, writable in-memory copy of the bundled root file system. Lost when the machine is.
     */
    public static BlockDevice createVolatileRootDisk() throws IOException {
        return ByteBufferBlockDevice.createFromStream(Buildroot.getRootFilesystem(), false);
    }

    /**
     * The bundled root file system, e.g. for installing it onto a new disk.
     */
    public static InputStream openRootFilesystemImage() {
        return Buildroot.getRootFilesystem();
    }

    // --------------------------------------------------------------------- //

    public void boot() throws MemoryAccessException {
        board.reset();
        loadFirmware();
        board.initialize();
        board.setRunning(true);
    }

    public void step(final int cycles) {
        board.step(cycles);
    }

    /**
     * Whether the machine runs; false once it powered off or asked to be restarted.
     */
    public boolean isRunning() {
        return board.isRunning();
    }

    /**
     * Whether the guest asked to be restarted. It stays stopped until booted again.
     */
    public boolean isRestarting() {
        return board.isRestarting();
    }

    public int getFrequency() {
        return board.getCpu().getFrequency();
    }

    public void setFrequency(final int value) {
        board.getCpu().setFrequency(value);
    }

    @Override
    public void close() throws Exception {
        memory.close();
        for (final BlockDevice disk : disks) {
            disk.close();
        }
    }

    // --------------------------------------------------------------------- //

    /**
     * Writes the complete machine state: memory contents and the state of the CPU and all devices.
     * Disk contents are not included, they live in their block devices.
     */
    public void saveState(final DataOutputStream output) throws IOException, MemoryAccessException {
        output.writeInt(STATE_VERSION);
        output.writeInt(memory.getLength());
        final ByteBuffer chunk = ByteBuffer.allocate(MEMORY_COPY_CHUNK);
        for (int offset = 0; offset < memory.getLength(); offset += MEMORY_COPY_CHUNK) {
            chunk.clear().limit(Math.min(MEMORY_COPY_CHUNK, memory.getLength() - offset));
            memory.load(offset, chunk);
            output.write(chunk.array(), 0, chunk.limit());
        }

        output.writeInt(diskDevices.length);
        BinarySerialization.serialize(output, board, R5Board.class);
        BinarySerialization.serialize(output, uart, UART16550A.class);
        BinarySerialization.serialize(output, rtc, GoldfishRTC.class);
        for (final VirtIOBlockDevice disk : diskDevices) {
            BinarySerialization.serialize(output, disk, VirtIOBlockDevice.class);
        }
        output.writeInt(networkDevices.length);
        for (final VirtIONetworkDevice network : networkDevices) {
            BinarySerialization.serialize(output, network, VirtIONetworkDevice.class);
        }
        BinarySerialization.serialize(output, busPorts, VirtIOConsoleDevice.class);
        BinarySerialization.serialize(output, builtin, VirtIOFileSystemDevice.class);
        window.saveState(output);
    }

    /**
     * Restores a state written by {@link #saveState}, in place of booting. The machine must have been
     * built with the same memory size and number of disks.
     */
    public void loadState(final DataInputStream input) throws IOException, MemoryAccessException {
        if (input.readInt() != STATE_VERSION) {
            throw new IOException("Unsupported machine state version.");
        }
        if (input.readInt() != memory.getLength()) {
            throw new IOException("Memory size changed.");
        }
        final ByteBuffer chunk = ByteBuffer.allocate(MEMORY_COPY_CHUNK);
        for (int offset = 0; offset < memory.getLength(); offset += MEMORY_COPY_CHUNK) {
            final int length = Math.min(MEMORY_COPY_CHUNK, memory.getLength() - offset);
            input.readFully(chunk.array(), 0, length);
            chunk.clear().limit(length);
            memory.store(offset, chunk);
        }

        if (input.readInt() != diskDevices.length) {
            throw new IOException("Disks changed.");
        }
        BinarySerialization.deserialize(input, R5Board.class, board);
        BinarySerialization.deserialize(input, UART16550A.class, uart);
        BinarySerialization.deserialize(input, GoldfishRTC.class, rtc);
        for (final VirtIOBlockDevice disk : diskDevices) {
            BinarySerialization.deserialize(input, VirtIOBlockDevice.class, disk);
        }
        if (input.readInt() != networkDevices.length) {
            throw new IOException("Network interfaces changed.");
        }
        for (final VirtIONetworkDevice network : networkDevices) {
            BinarySerialization.deserialize(input, VirtIONetworkDevice.class, network);
        }
        BinarySerialization.deserialize(input, VirtIOConsoleDevice.class, busPorts);
        BinarySerialization.deserialize(input, VirtIOFileSystemDevice.class, builtin);
        window.loadState(input);
    }

    // --------------------------------------------------------------------- //

    /**
     * The next byte written to the console, or -1 when there is none.
     */
    public int readConsole() {
        return uart.read();
    }

    public boolean canWriteConsole() {
        return uart.canPutByte();
    }

    public void writeConsole(final byte value) {
        uart.putByte(value);
    }

    public int getNetworkCount() {
        return networkDevices.length;
    }

    /**
     * The next Ethernet frame the guest sent on the given interface, or null when there is none.
     */
    @Nullable
    public byte[] readFrame(final int network) {
        return networkDevices[network].readEthernetFrame();
    }

    /**
     * Hands an Ethernet frame to the guest on the given interface. Dropped if its queue is full.
     */
    public void writeFrame(final int network, final byte[] frame) {
        networkDevices[network].writeEthernetFrame(frame);
    }

    public SerialDevice getRpcPort() {
        return busPorts.getPort(0);
    }

    public SerialDevice getBlobPort() {
        return busPorts.getPort(1);
    }

    public SerialDevice getEventPort() {
        return busPorts.getPort(2);
    }

    public ComponentWindow getWindow() {
        return window;
    }

    // --------------------------------------------------------------------- //

    private void loadFirmware() throws MemoryAccessException {
        for (int address = 0; address < firmware.length; address++) {
            memory.store(address, firmware[address], Sizes.SIZE_8_LOG2);
        }
    }

    // Read-only, so all machines share one.
    private static synchronized FileSystem getScripts() throws IOException {
        if (scripts == null) {
            try (final InputStream stream = RiscvMachine.class.getResourceAsStream(SCRIPTS_RESOURCE)) {
                if (stream == null) {
                    throw new IOException("Missing " + SCRIPTS_RESOURCE + ".");
                }
                scripts = new ZipStreamFileSystem(stream);
            }
        }
        return scripts;
    }
}
