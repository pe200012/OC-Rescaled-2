package li.cil.oc.riscv;

import li.cil.sedna.Sedna;
import li.cil.sedna.api.Sizes;
import li.cil.sedna.api.device.BlockDevice;
import li.cil.sedna.api.device.PhysicalMemory;
import li.cil.sedna.api.device.serial.SerialDevice;
import li.cil.sedna.api.memory.MemoryAccessException;
import li.cil.sedna.buildroot.Buildroot;
import li.cil.sedna.device.block.ByteBufferBlockDevice;
import li.cil.sedna.device.memory.Memory;
import li.cil.sedna.device.rtc.GoldfishRTC;
import li.cil.sedna.device.rtc.SystemTimeRealTimeCounter;
import li.cil.sedna.device.serial.UART16550A;
import li.cil.sedna.device.virtio.VirtIOBlockDevice;
import li.cil.sedna.device.virtio.VirtIOConsoleDevice;
import li.cil.sedna.device.virtio.VirtIOFileSystemDevice;
import li.cil.sedna.fs.FileSystem;
import li.cil.sedna.fs.ZipStreamFileSystem;
import li.cil.sedna.riscv.R5Board;

import java.io.IOException;
import java.io.InputStream;

/**
 * A RISC-V board running the bundled Linux image: RAM, a UART console, a real time clock and one
 * root disk. Independent of Minecraft, so it can be booted from tests.
 */
public final class RiscvMachine {
    private static final long MEMORY_ADDRESS = 0x80000000L;
    private static final String BOOT_ARGUMENTS = "root=/dev/vda rw";

    private static final int ROOT_DISK_INTERRUPT = 0x1;
    private static final int BUS_INTERRUPT = 0x3;
    private static final int SCRIPTS_INTERRUPT = 0x5;
    private static final int UART_INTERRUPT = 0xA;
    private static final int RTC_INTERRUPT = 0xB;

    // Names the guest's device bus libraries look for, shared with OpenComputers II.
    private static final String[] BUS_PORT_NAMES = {"oc2.rpc.0", "oc2.blob.0", "oc2.event.0"};
    private static final String SCRIPTS_TAG = "builtin";
    private static final String SCRIPTS_RESOURCE = "/li/cil/oc/riscv/scripts.zip";

    private static byte[] firmware;
    private static FileSystem scripts;

    private final R5Board board = new R5Board();
    private final PhysicalMemory memory;
    private final UART16550A uart = new UART16550A();
    private final VirtIOConsoleDevice busPorts;

    public RiscvMachine(final int memorySize, final BlockDevice rootDisk) throws IOException {
        Sedna.initialize();

        memory = Memory.create(memorySize);
        final GoldfishRTC rtc = new GoldfishRTC(SystemTimeRealTimeCounter.get());
        final VirtIOBlockDevice disk = new VirtIOBlockDevice(board.getMemoryMap(), rootDisk);
        busPorts = new VirtIOConsoleDevice(board.getMemoryMap(), BUS_PORT_NAMES);
        final VirtIOFileSystemDevice builtin = new VirtIOFileSystemDevice(board.getMemoryMap(), SCRIPTS_TAG, getScripts());

        uart.getInterrupt().set(UART_INTERRUPT, board.getInterruptController());
        rtc.getInterrupt().set(RTC_INTERRUPT, board.getInterruptController());
        disk.getInterrupt().set(ROOT_DISK_INTERRUPT, board.getInterruptController());
        busPorts.getInterrupt().set(BUS_INTERRUPT, board.getInterruptController());
        builtin.getInterrupt().set(SCRIPTS_INTERRUPT, board.getInterruptController());

        if (!board.addDevice(MEMORY_ADDRESS, memory)) {
            throw new IllegalStateException("Failed mapping memory.");
        }
        if (board.addDevice(uart).isEmpty() || board.addDevice(rtc).isEmpty() || board.addDevice(disk).isEmpty()
            || board.addDevice(busPorts).isEmpty() || board.addDevice(builtin).isEmpty()) {
            throw new IllegalStateException("Failed mapping devices.");
        }

        board.setBootArguments(BOOT_ARGUMENTS);
        board.setStandardOutputDevice(uart);
        board.setFirmwareSize(Buildroot.getSednaFirmwareRegionSize());
    }

    /**
     * A fresh, writable copy of the bundled root file system.
     */
    public static BlockDevice createDefaultRootDisk() throws IOException {
        return ByteBufferBlockDevice.createFromStream(Buildroot.getRootFilesystem(), false);
    }

    // --------------------------------------------------------------------- //

    public void boot() throws IOException, MemoryAccessException {
        board.reset();
        loadFirmware();
        board.initialize();
        board.setRunning(true);
    }

    public void step(final int cycles) throws IOException, MemoryAccessException {
        board.step(cycles);
        if (board.isRestarting()) {
            loadFirmware();
            board.initialize();
        }
    }

    public boolean isRunning() {
        return board.isRunning();
    }

    public int getFrequency() {
        return board.getCpu().getFrequency();
    }

    public void setFrequency(final int value) {
        board.getCpu().setFrequency(value);
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

    // --------------------------------------------------------------------- //

    public SerialDevice getRpcPort() {
        return busPorts.getPort(0);
    }

    public SerialDevice getBlobPort() {
        return busPorts.getPort(1);
    }

    public SerialDevice getEventPort() {
        return busPorts.getPort(2);
    }

    // --------------------------------------------------------------------- //

    private void loadFirmware() throws IOException, MemoryAccessException {
        final byte[] program = getFirmware();
        for (int address = 0; address < program.length; address++) {
            memory.store(address, program[address], Sizes.SIZE_8_LOG2);
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

    private static synchronized byte[] getFirmware() throws IOException {
        if (firmware == null) {
            try (final InputStream stream = Buildroot.getSednaFirmware()) {
                firmware = stream.readAllBytes();
            }
        }
        return firmware;
    }
}
