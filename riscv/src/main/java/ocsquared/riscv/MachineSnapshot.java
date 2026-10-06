package ocsquared.riscv;

import li.cil.ceres.BinarySerialization;
import ocsquared.riscv.bus.DeviceBus;
import ocsquared.riscv.terminal.Terminal;
import li.cil.sedna.api.memory.MemoryAccessException;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

/**
 * Saves a running machine to a file and restores it: memory, CPU and device state, the device bus
 * and the console. Memory is mostly zeros, so the file is compressed with the fastest setting.
 */
public final class MachineSnapshot {
    private static final int MAGIC = 0x4F435256; // "OCRV"
    private static final int BUFFER_SIZE = 256 * 1024;

    private MachineSnapshot() {
    }

    /**
     * Writes a snapshot tagged with {@code id}, replacing the file only once it is complete.
     */
    public static void write(final Path file, final long id, final RiscvMachine machine, final DeviceBus bus, final Terminal terminal)
        throws IOException, MemoryAccessException {
        Files.createDirectories(file.getParent());
        final Path temporary = file.resolveSibling(file.getFileName() + ".tmp");
        try (final OutputStream stream = Files.newOutputStream(temporary)) {
            final DataOutputStream header = new DataOutputStream(stream);
            header.writeInt(MAGIC);
            header.writeLong(id);
            header.flush();

            final Deflater deflater = new Deflater(Deflater.BEST_SPEED);
            try {
                final DataOutputStream output = new DataOutputStream(new BufferedOutputStream(
                    new DeflaterOutputStream(stream, deflater, BUFFER_SIZE), BUFFER_SIZE));
                machine.saveState(output);
                bus.saveState(output);
                BinarySerialization.serialize(output, terminal, Terminal.class);
                output.close();
            } finally {
                deflater.end();
            }
        }
        Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    }

    /**
     * Restores the snapshot tagged with {@code id} into freshly built objects. Returns false if
     * there is no such snapshot, in which case nothing was changed.
     */
    public static boolean read(final Path file, final long id, final RiscvMachine machine, final DeviceBus bus, final Terminal terminal)
        throws IOException, MemoryAccessException {
        if (!Files.isRegularFile(file)) {
            return false;
        }
        try (final InputStream stream = Files.newInputStream(file)) {
            final DataInputStream header = new DataInputStream(stream);
            if (header.readInt() != MAGIC || header.readLong() != id) {
                return false;
            }

            final DataInputStream input = new DataInputStream(new BufferedInputStream(
                new InflaterInputStream(stream), BUFFER_SIZE));
            machine.loadState(input);
            bus.loadState(input);
            BinarySerialization.deserialize(input, Terminal.class, terminal);
        }
        return true;
    }

    public static void delete(final Path file) throws IOException {
        Files.deleteIfExists(file);
    }
}
