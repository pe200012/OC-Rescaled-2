package li.cil.oc.riscv.bus;

import javax.annotation.Nullable;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The part of CBOR (RFC 8949) the component window speaks: integers, floats, byte and text strings,
 * arrays, maps, booleans and null. Tags are skipped, as are lengths that do not fit into an int.
 */
public final class Cbor {
    private static final int MAJOR_UNSIGNED = 0;
    private static final int MAJOR_NEGATIVE = 1;
    private static final int MAJOR_BYTES = 2;
    private static final int MAJOR_TEXT = 3;
    private static final int MAJOR_ARRAY = 4;
    private static final int MAJOR_MAP = 5;
    private static final int MAJOR_TAG = 6;
    private static final int MAJOR_SIMPLE = 7;

    private static final int INDEFINITE = 31;
    private static final int BREAK = 0xFF;
    private static final int MAX_DEPTH = 32;

    private Cbor() {
    }

    // --------------------------------------------------------------------- //

    /**
     * Encodes values as components hand them out. Integral numbers become integers, tables with
     * keys 1..n become arrays, and anything unknown becomes its string form.
     */
    public static byte[] encode(@Nullable final Object value) {
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        write(output, value, 0);
        return output.toByteArray();
    }

    /**
     * Encodes values as the elements of one array.
     */
    public static byte[] encodeArray(final List<?> values) {
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        writeHead(output, MAJOR_ARRAY, values.size());
        for (final Object value : values) {
            write(output, value, 1);
        }
        return output.toByteArray();
    }

    private static void write(final ByteArrayOutputStream output, @Nullable final Object value, final int depth) {
        if (depth > MAX_DEPTH || value == null) {
            output.write(0xF6);
        } else if (value instanceof Boolean b) {
            output.write(b ? 0xF5 : 0xF4);
        } else if (value instanceof Character c) {
            writeInteger(output, c);
        } else if (value instanceof Byte || value instanceof Short || value instanceof Integer || value instanceof Long) {
            writeInteger(output, ((Number) value).longValue());
        } else if (value instanceof Number n) {
            final double d = n.doubleValue();
            if (d == Math.rint(d) && Math.abs(d) < 0x1p63) {
                writeInteger(output, (long) d);
            } else {
                output.write(0xFB);
                writeBigEndian(output, Double.doubleToLongBits(d), 8);
            }
        } else if (value instanceof String s) {
            final byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
            writeHead(output, MAJOR_TEXT, bytes.length);
            output.writeBytes(bytes);
        } else if (value instanceof byte[] bytes) {
            writeHead(output, MAJOR_BYTES, bytes.length);
            output.writeBytes(bytes);
        } else if (value instanceof Object[] array) {
            writeHead(output, MAJOR_ARRAY, array.length);
            for (final Object element : array) {
                write(output, element, depth + 1);
            }
        } else if (value instanceof Collection<?> collection) {
            writeHead(output, MAJOR_ARRAY, collection.size());
            for (final Object element : collection) {
                write(output, element, depth + 1);
            }
        } else if (value instanceof Map<?, ?> map) {
            final List<Object> sequence = asSequence(map);
            if (sequence != null) {
                write(output, sequence, depth);
                return;
            }
            writeHead(output, MAJOR_MAP, map.size());
            for (final Map.Entry<?, ?> entry : map.entrySet()) {
                write(output, entry.getKey(), depth + 1);
                write(output, entry.getValue(), depth + 1);
            }
        } else {
            write(output, value.toString(), depth);
        }
    }

    // A table with keys 1..n is a list.
    @Nullable
    private static List<Object> asSequence(final Map<?, ?> map) {
        if (map.isEmpty()) {
            return null;
        }
        final List<Object> sequence = new ArrayList<>(map.size());
        for (int i = 1; i <= map.size(); i++) {
            final Object key = map.containsKey(i) ? (Object) i
                : map.containsKey((long) i) ? (Object) (long) i
                : map.containsKey((double) i) ? (Object) (double) i
                : null;
            if (key == null) {
                return null;
            }
            sequence.add(map.get(key));
        }
        return sequence;
    }

    private static void writeInteger(final ByteArrayOutputStream output, final long value) {
        if (value >= 0) {
            writeHead(output, MAJOR_UNSIGNED, value);
        } else {
            writeHead(output, MAJOR_NEGATIVE, -1 - value);
        }
    }

    private static void writeHead(final ByteArrayOutputStream output, final int major, final long argument) {
        final int type = major << 5;
        if (argument < 24) {
            output.write(type | (int) argument);
        } else if (argument <= 0xFF) {
            output.write(type | 24);
            output.write((int) argument);
        } else if (argument <= 0xFFFF) {
            output.write(type | 25);
            writeBigEndian(output, argument, 2);
        } else if (argument <= 0xFFFFFFFFL) {
            output.write(type | 26);
            writeBigEndian(output, argument, 4);
        } else {
            output.write(type | 27);
            writeBigEndian(output, argument, 8);
        }
    }

    private static void writeBigEndian(final ByteArrayOutputStream output, final long value, final int length) {
        for (int i = length - 1; i >= 0; i--) {
            output.write((int) (value >>> (i * 8)));
        }
    }

    // --------------------------------------------------------------------- //

    /**
     * Decodes one value: integers to {@link Long}, floats to {@link Double}, text to {@link String},
     * bytes to {@code byte[]}, arrays to {@link List}, maps to {@link Map}.
     *
     * @throws IllegalArgumentException if the data is not well-formed or has data after the value.
     */
    @Nullable
    public static Object decode(final byte[] data, final int length) {
        final Reader reader = new Reader(data, length);
        final Object value = reader.read(0);
        if (reader.position != length) {
            throw new IllegalArgumentException("trailing data");
        }
        return value;
    }

    private static final class Reader {
        private final byte[] data;
        private final int length;
        private int position;

        Reader(final byte[] data, final int length) {
            this.data = data;
            this.length = length;
        }

        @Nullable
        Object read(final int depth) {
            if (depth > MAX_DEPTH) {
                throw new IllegalArgumentException("nested too deeply");
            }
            final int initial = next();
            final int major = initial >>> 5;
            final int info = initial & 0x1F;
            switch (major) {
                case MAJOR_UNSIGNED -> {
                    final long value = argument(info);
                    if (value < 0) {
                        throw new IllegalArgumentException("integer out of range");
                    }
                    return value;
                }
                case MAJOR_NEGATIVE -> {
                    final long value = argument(info);
                    if (value < 0) {
                        throw new IllegalArgumentException("integer out of range");
                    }
                    return -1 - value;
                }
                case MAJOR_BYTES -> {
                    return info == INDEFINITE ? chunks(MAJOR_BYTES) : bytes(length(info));
                }
                case MAJOR_TEXT -> {
                    final byte[] bytes = info == INDEFINITE ? chunks(MAJOR_TEXT) : bytes(length(info));
                    return new String(bytes, StandardCharsets.UTF_8);
                }
                case MAJOR_ARRAY -> {
                    final List<Object> list = new ArrayList<>();
                    if (info == INDEFINITE) {
                        while (peek() != BREAK) {
                            list.add(read(depth + 1));
                        }
                        position++;
                    } else {
                        final int count = length(info);
                        for (int i = 0; i < count; i++) {
                            list.add(read(depth + 1));
                        }
                    }
                    return list;
                }
                case MAJOR_MAP -> {
                    final Map<Object, Object> map = new LinkedHashMap<>();
                    if (info == INDEFINITE) {
                        while (peek() != BREAK) {
                            map.put(read(depth + 1), read(depth + 1));
                        }
                        position++;
                    } else {
                        final int count = length(info);
                        for (int i = 0; i < count; i++) {
                            map.put(read(depth + 1), read(depth + 1));
                        }
                    }
                    return map;
                }
                case MAJOR_TAG -> {
                    argument(info);
                    return read(depth + 1);
                }
                default -> {
                    return simple(info);
                }
            }
        }

        @Nullable
        private Object simple(final int info) {
            return switch (info) {
                case 20 -> false;
                case 21 -> true;
                case 22, 23 -> null;
                case 25 -> (double) Float.float16ToFloat((short) bigEndian(2));
                case 26 -> (double) Float.intBitsToFloat((int) bigEndian(4));
                case 27 -> Double.longBitsToDouble(bigEndian(8));
                default -> throw new IllegalArgumentException("unsupported simple value");
            };
        }

        private byte[] chunks(final int major) {
            final ByteArrayOutputStream output = new ByteArrayOutputStream();
            while (peek() != BREAK) {
                final int initial = next();
                if (initial >>> 5 != major || (initial & 0x1F) == INDEFINITE) {
                    throw new IllegalArgumentException("malformed string");
                }
                output.writeBytes(bytes(length(initial & 0x1F)));
            }
            position++;
            return output.toByteArray();
        }

        private int length(final int info) {
            final long value = argument(info);
            if (value < 0 || value > length - position) {
                throw new IllegalArgumentException("length out of range");
            }
            return (int) value;
        }

        private long argument(final int info) {
            if (info < 24) {
                return info;
            }
            return switch (info) {
                case 24 -> bigEndian(1);
                case 25 -> bigEndian(2);
                case 26 -> bigEndian(4);
                case 27 -> bigEndian(8);
                default -> throw new IllegalArgumentException("malformed value");
            };
        }

        private byte[] bytes(final int count) {
            if (count > length - position) {
                throw new IllegalArgumentException("truncated");
            }
            final byte[] result = new byte[count];
            System.arraycopy(data, position, result, 0, count);
            position += count;
            return result;
        }

        private long bigEndian(final int count) {
            long value = 0;
            for (int i = 0; i < count; i++) {
                value = (value << 8) | next();
            }
            return value;
        }

        private int peek() {
            if (position >= length) {
                throw new IllegalArgumentException("truncated");
            }
            return data[position] & 0xFF;
        }

        private int next() {
            final int value = peek();
            position++;
            return value;
        }
    }
}
