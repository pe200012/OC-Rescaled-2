package li.cil.oc.riscv;

import li.cil.sedna.api.device.BlockDevice;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * A disk image in a file of fixed size, read and written in place.
 */
public final class FileBlockDevice implements BlockDevice {
    private final FileChannel channel;
    private final long capacity;
    private final boolean readonly;

    private FileBlockDevice(final FileChannel channel, final long capacity, final boolean readonly) {
        this.channel = channel;
        this.capacity = capacity;
        this.readonly = readonly;
    }

    /**
     * Opens the image at the given path, creating it filled with zeros if it does not exist yet.
     */
    public static FileBlockDevice open(final Path path, final long capacity) throws IOException {
        Files.createDirectories(path.getParent());
        final FileChannel channel = FileChannel.open(path, StandardOpenOption.CREATE, StandardOpenOption.READ, StandardOpenOption.WRITE);
        try {
            if (channel.size() < capacity) {
                // Writing the last byte sizes the file; the rest reads back as zeros.
                channel.write(ByteBuffer.wrap(new byte[1]), capacity - 1);
            }
            return new FileBlockDevice(channel, capacity, false);
        } catch (final IOException e) {
            channel.close();
            throw e;
        }
    }

    /**
     * Copies an image into the start of this device, e.g. to install a root file system.
     */
    public void writeImage(final InputStream image) throws IOException {
        final byte[] buffer = new byte[64 * 1024];
        long position = 0;
        int count;
        while ((count = image.read(buffer)) > 0) {
            if (position + count > capacity) {
                throw new IOException("Image does not fit the device.");
            }
            channel.write(ByteBuffer.wrap(buffer, 0, count), position);
            position += count;
        }
    }

    // --------------------------------------------------------------------- //

    @Override
    public void flush() {
        try {
            channel.force(false);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public boolean isReadonly() {
        return readonly;
    }

    @Override
    public long getCapacity() {
        return capacity;
    }

    @Override
    public InputStream getInputStream(final long offset) {
        return new InputStream() {
            private long position = offset;

            @Override
            public int read() throws IOException {
                final byte[] single = new byte[1];
                return read(single, 0, 1) < 0 ? -1 : single[0] & 0xFF;
            }

            @Override
            public int read(final byte[] buffer, final int start, final int length) throws IOException {
                final int count = (int) Math.min(length, capacity - position);
                if (count <= 0) {
                    return -1;
                }
                final ByteBuffer target = ByteBuffer.wrap(buffer, start, count);
                while (target.hasRemaining()) {
                    if (channel.read(target, position + target.position() - start) < 0) {
                        break;
                    }
                }
                final int read = target.position() - start;
                position += read;
                return read;
            }
        };
    }

    @Override
    public OutputStream getOutputStream(final long offset) {
        if (readonly) {
            throw new UnsupportedOperationException();
        }
        return new OutputStream() {
            private long position = offset;

            @Override
            public void write(final int value) throws IOException {
                write(new byte[]{(byte) value}, 0, 1);
            }

            @Override
            public void write(final byte[] buffer, final int start, final int length) throws IOException {
                if (position + length > capacity) {
                    throw new IOException("Write past the end of the device.");
                }
                final ByteBuffer source = ByteBuffer.wrap(buffer, start, length);
                while (source.hasRemaining()) {
                    channel.write(source, position + source.position() - start);
                }
                position += length;
            }
        };
    }

    @Override
    public void close() {
        try {
            channel.close();
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
