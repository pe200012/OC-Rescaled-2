package li.cil.oc.riscv;

import li.cil.sedna.api.Sizes;
import li.cil.sedna.api.device.MemoryMappedDevice;

import javax.annotation.Nullable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * A linear framebuffer of 32-bit pixels, little-endian 0x00RRGGBB words, row after row. Linux
 * drives it as a simple-framebuffer, /dev/fb0. Remembers which rows were written, so that only
 * those need to be sent to the screen showing it.
 * <p>
 * Written by the machine while it steps; read between steps, on the same thread.
 */
public final class Framebuffer implements MemoryMappedDevice {
    public static final int WIDTH = 320;
    public static final int HEIGHT = 192;
    public static final int STRIDE = WIDTH * Integer.BYTES;
    // As Linux names it, see include/linux/platform_data/simplefb.h.
    public static final String FORMAT = "x8r8g8b8";

    /**
     * Rows of pixels, as 0xRRGGBB colors, row after row.
     */
    public record Rows(int first, int count, int[] colors) {
    }

    private final ByteBuffer pixels = ByteBuffer.allocate(STRIDE * HEIGHT).order(ByteOrder.LITTLE_ENDIAN);
    // The rows written since the changes were last taken; none while the first is past the last.
    private int firstChangedRow = HEIGHT;
    private int lastChangedRow = -1;

    /**
     * Whether rows were written since the changes were last taken.
     */
    public boolean hasChanges() {
        return firstChangedRow <= lastChangedRow;
    }

    /**
     * Counts all rows as changed, e.g. so the whole picture is sent to a new screen.
     */
    public void markChanged() {
        firstChangedRow = 0;
        lastChangedRow = HEIGHT - 1;
    }

    /**
     * Forgets about the rows written so far, e.g. while no screen shows them.
     */
    public void discardChanges() {
        firstChangedRow = HEIGHT;
        lastChangedRow = -1;
    }

    /**
     * The rows written since the last call, or null if there are none.
     */
    @Nullable
    public Rows takeChanges() {
        if (!hasChanges()) {
            return null;
        }
        final int first = firstChangedRow;
        final int count = lastChangedRow - firstChangedRow + 1;
        discardChanges();

        final int[] colors = new int[count * WIDTH];
        for (int i = 0; i < colors.length; i++) {
            colors[i] = pixels.getInt((first * WIDTH + i) * Integer.BYTES) & 0xFFFFFF;
        }
        return new Rows(first, count, colors);
    }

    public void save(final DataOutputStream output) throws IOException {
        output.write(pixels.array());
    }

    public void load(final DataInputStream input) throws IOException {
        input.readFully(pixels.array());
    }

    // --------------------------------------------------------------------- //

    @Override
    public int getLength() {
        return pixels.capacity();
    }

    @Override
    public long load(final int offset, final int sizeLog2) {
        return switch (sizeLog2) {
            case Sizes.SIZE_8_LOG2 -> pixels.get(offset);
            case Sizes.SIZE_16_LOG2 -> pixels.getShort(offset);
            case Sizes.SIZE_32_LOG2 -> pixels.getInt(offset);
            case Sizes.SIZE_64_LOG2 -> pixels.getLong(offset);
            default -> throw new IllegalArgumentException();
        };
    }

    @Override
    public void store(final int offset, final long value, final int sizeLog2) {
        switch (sizeLog2) {
            case Sizes.SIZE_8_LOG2 -> pixels.put(offset, (byte) value);
            case Sizes.SIZE_16_LOG2 -> pixels.putShort(offset, (short) value);
            case Sizes.SIZE_32_LOG2 -> pixels.putInt(offset, (int) value);
            case Sizes.SIZE_64_LOG2 -> pixels.putLong(offset, value);
            default -> throw new IllegalArgumentException();
        }
        firstChangedRow = Math.min(firstChangedRow, offset / STRIDE);
        // Unaligned stores may reach into the next row.
        lastChangedRow = Math.max(lastChangedRow, (offset + (1 << sizeLog2) - 1) / STRIDE);
    }
}
