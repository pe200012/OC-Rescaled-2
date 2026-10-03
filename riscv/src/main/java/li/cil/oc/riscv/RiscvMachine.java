package li.cil.oc.riscv;

import li.cil.sedna.Sedna;
import li.cil.sedna.api.Sizes;
import li.cil.sedna.api.device.BlockDevice;
import li.cil.sedna.api.device.PhysicalMemory;
import li.cil.sedna.api.memory.MemoryAccessException;
import li.cil.sedna.buildroot.Buildroot;
import li.cil.sedna.device.block.ByteBufferBlockDevice;
import li.cil.sedna.device.memory.Memory;
import li.cil.sedna.device.rtc.GoldfishRTC;
import li.cil.sedna.device.rtc.SystemTimeRealTimeCounter;
import li.cil.sedna.device.serial.UART16550A;
import li.cil.sedna.device.virtio.VirtIOBlockDevice;
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
    private static final int UART_INTERRUPT = 0xA;
    private static final int RTC_INTERRUPT = 0xB;

    private static byte[] firmware;

    private final R5Board board = new R5Board();
    private final PhysicalMemory memory;
    private final UART16550A uart = new UART16550A();

    public RiscvMachine(final int memorySize, final BlockDevice rootDisk) {
        Sedna.initialize();

        memory = Memory.create(memorySize);
        final GoldfishRTC rtc = new GoldfishRTC(SystemTimeRealTimeCounter.get());
        final VirtIOBlockDevice disk = new VirtIOBlockDevice(board.getMemoryMap(), rootDisk);

        uart.getInterrupt().set(UART_INTERRUPT, board.getInterruptController());
        rtc.getInterrupt().set(RTC_INTERRUPT, board.getInterruptController());
        disk.getInterrupt().set(ROOT_DISK_INTERRUPT, board.getInterruptController());

        if (!board.addDevice(MEMORY_ADDRESS, memory)) {
            throw new IllegalStateException("Failed mapping memory.");
        }
        if (board.addDevice(uart).isEmpty() || board.addDevice(rtc).isEmpty() || board.addDevice(disk).isEmpty()) {
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

    private void loadFirmware() throws IOException, MemoryAccessException {
        final byte[] program = getFirmware();
        for (int address = 0; address < program.length; address++) {
            memory.store(address, program[address], Sizes.SIZE_8_LOG2);
        }
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
