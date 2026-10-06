package ocsquared.riscv.bus;

import li.cil.sedna.api.Interrupt;
import li.cil.sedna.api.device.InterruptSource;
import li.cil.sedna.api.device.MemoryMappedDevice;

import javax.annotation.Nullable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.IntSupplier;

/**
 * Lets programs without an operating system use components: a page of registers and a buffer at a
 * fixed address, carrying CBOR. The guest writes a request into the buffer, its length into
 * {@code LENGTH} and a command into {@code COMMAND}, then waits for {@code STATUS} to leave
 * {@link #STATUS_BUSY}. The reply is in the buffer, {@code LENGTH} bytes long.
 * <ul>
 *     <li>{@link #COMMAND_LIST}: no request; replies {@code [address, type, address, type, ...]}.</li>
 *     <li>{@link #COMMAND_METHODS}: request {@code address}; replies {@code [name, doc, name, doc, ...]}.</li>
 *     <li>{@link #COMMAND_INVOKE}: request {@code [address, method, args...]}; replies {@code [results...]}.</li>
 *     <li>{@link #COMMAND_SIGNAL}: no request; replies {@code [name, args...]} for the oldest queued
 *     signal, or nothing ({@code LENGTH} 0) if there is none. Signals are only queued while
 *     {@link #CONTROL_QUEUE} is set.</li>
 *     <li>{@link #COMMAND_PANIC}: request {@code message}; crashes the machine with that message.</li>
 * </ul>
 * Errors set {@link #STATUS_ERROR} and reply with a message. Linux sees the window as a UIO device.
 */
public final class ComponentWindow implements MemoryMappedDevice, InterruptSource {
    public static final int REGISTERS_SIZE = 0x1000;
    public static final int BUFFER_SIZE = 0x20000;
    public static final int LENGTH = REGISTERS_SIZE + BUFFER_SIZE;

    public static final int MAGIC = 0x3142434F; // "OCB1"

    // Registers, 32 bits each.
    private static final int REG_MAGIC = 0x00;
    private static final int REG_BUFFER_SIZE = 0x04;
    private static final int REG_COMMAND = 0x08;
    private static final int REG_STATUS = 0x0C;
    private static final int REG_LENGTH = 0x10;
    private static final int REG_CONTROL = 0x14;
    private static final int REG_SIGNALS = 0x18;
    private static final int REG_GENERATION = 0x1C;
    private static final int REG_TIMEBASE = 0x20;

    public static final int COMMAND_LIST = 1;
    public static final int COMMAND_METHODS = 2;
    public static final int COMMAND_INVOKE = 3;
    public static final int COMMAND_SIGNAL = 4;
    public static final int COMMAND_PANIC = 5;

    public static final int STATUS_IDLE = 0;
    public static final int STATUS_BUSY = 1;
    public static final int STATUS_DONE = 2;
    public static final int STATUS_ERROR = 3;

    public static final int CONTROL_QUEUE = 1; // Queue signals.
    public static final int CONTROL_INTERRUPT = 2; // Raise the interrupt while signals are queued.

    private static final int SIGNALS_OVERFLOW = 1 << 31; // In REG_SIGNALS: some were dropped.
    private static final int MAX_SIGNALS = 256;
    private static final int STATE_VERSION = 1;

    private static final DeviceBus.Devices NO_DEVICES = new DeviceBus.Devices() {
        @Override
        public int generation() {
            return 0;
        }

        @Override
        public List<DeviceBus.DeviceInfo> list() {
            return List.of();
        }

        @Nullable
        @Override
        public List<DeviceBus.MethodInfo> methods(final UUID device) {
            return null;
        }

        @Override
        public Object[] invoke(final UUID device, final String method, final Object[] arguments, final boolean isMainThread) throws Exception {
            throw new IllegalArgumentException("no such component");
        }
    };

    private final Interrupt interrupt = new Interrupt();
    private final IntSupplier timebase;
    private DeviceBus.Devices devices = NO_DEVICES;

    private final byte[] buffer = new byte[BUFFER_SIZE];
    private int status;
    private int length;
    private int control;
    private boolean overflow;
    private final ArrayDeque<byte[]> signals = new ArrayDeque<>();
    @Nullable
    private volatile byte[] mainThreadCall; // The request of an invocation waiting for the server thread.
    @Nullable
    private String panic;

    /**
     * @param timebase the frequency of the machine's timer, for programs to measure time with.
     */
    public ComponentWindow(final IntSupplier timebase) {
        this.timebase = timebase;
    }

    public Interrupt getInterrupt() {
        return interrupt;
    }

    public void setDevices(final DeviceBus.Devices devices) {
        this.devices = devices;
    }

    /**
     * Queues a signal for the guest, if it asked for them. Called from the machine's thread.
     */
    public void sendSignal(final String name, final Object[] args) {
        if ((control & CONTROL_QUEUE) == 0) {
            return;
        }
        if (signals.size() >= MAX_SIGNALS) {
            overflow = true;
            return;
        }
        final List<Object> values = new ArrayList<>(args.length + 1);
        values.add(name);
        values.addAll(Arrays.asList(args));
        signals.add(Cbor.encodeArray(values));
        updateInterrupt();
    }

    public boolean hasMainThreadCall() {
        return mainThreadCall != null;
    }

    /**
     * Runs the invocation that needs the server thread. Called from the server thread, while the
     * machine is not running.
     */
    public void runMainThreadCall() {
        final byte[] request = mainThreadCall;
        if (request == null) {
            return;
        }
        mainThreadCall = null;
        invoke(request, request.length, true);
    }

    /**
     * The message the guest panicked with, once; null if it did not.
     */
    @Nullable
    public String takePanic() {
        final String message = panic;
        panic = null;
        return message;
    }

    // --------------------------------------------------------------------- //

    public void saveState(final DataOutputStream output) throws IOException {
        output.writeInt(STATE_VERSION);
        output.writeInt(status);
        output.writeInt(length);
        output.writeInt(control);
        output.writeBoolean(overflow);
        output.writeInt(buffer.length);
        output.write(buffer);
        output.writeInt(signals.size());
        for (final byte[] signal : signals) {
            output.writeInt(signal.length);
            output.write(signal);
        }
        final byte[] call = mainThreadCall;
        output.writeInt(call != null ? call.length : -1);
        if (call != null) {
            output.write(call);
        }
    }

    public void loadState(final DataInputStream input) throws IOException {
        if (input.readInt() != STATE_VERSION) {
            throw new IOException("Unsupported component window state version.");
        }
        status = input.readInt();
        length = input.readInt();
        control = input.readInt();
        overflow = input.readBoolean();
        if (input.readInt() != buffer.length) {
            throw new IOException("Component window size changed.");
        }
        input.readFully(buffer);
        signals.clear();
        final int count = input.readInt();
        for (int i = 0; i < count; i++) {
            final byte[] signal = new byte[input.readInt()];
            input.readFully(signal);
            signals.add(signal);
        }
        final int callLength = input.readInt();
        if (callLength >= 0) {
            final byte[] call = new byte[callLength];
            input.readFully(call);
            mainThreadCall = call;
        } else {
            mainThreadCall = null;
        }
        updateInterrupt();
    }

    // --------------------------------------------------------------------- //

    @Override
    public int getLength() {
        return LENGTH;
    }

    @Override
    public Iterable<Interrupt> getInterrupts() {
        return List.of(interrupt);
    }

    @Override
    public long load(final int offset, final int sizeLog2) {
        final int size = 1 << sizeLog2;
        if (offset >= REGISTERS_SIZE) {
            final int index = offset - REGISTERS_SIZE;
            long value = 0;
            for (int i = size - 1; i >= 0; i--) {
                value = (value << 8) | (buffer[index + i] & 0xFF);
            }
            return value;
        }
        // Registers are 32 bits wide; wider loads see the next one in the upper half.
        long value = 0;
        for (int i = size - 1; i >= 0; i--) {
            final int address = offset + i;
            value = (value << 8) | ((readRegister(address & ~3) >>> ((address & 3) * 8)) & 0xFF);
        }
        return value;
    }

    @Override
    public void store(final int offset, final long value, final int sizeLog2) {
        final int size = 1 << sizeLog2;
        if (offset >= REGISTERS_SIZE) {
            final int index = offset - REGISTERS_SIZE;
            for (int i = 0; i < size; i++) {
                buffer[index + i] = (byte) (value >>> (i * 8));
            }
            return;
        }
        writeRegister(offset & ~3, (int) value);
        if (size == 8) {
            writeRegister((offset & ~3) + 4, (int) (value >>> 32));
        }
    }

    // --------------------------------------------------------------------- //

    private int readRegister(final int offset) {
        return switch (offset) {
            case REG_MAGIC -> MAGIC;
            case REG_BUFFER_SIZE -> BUFFER_SIZE;
            case REG_STATUS -> mainThreadCall != null ? STATUS_BUSY : status;
            case REG_LENGTH -> length;
            case REG_CONTROL -> control;
            case REG_SIGNALS -> signals.size() | (overflow ? SIGNALS_OVERFLOW : 0);
            case REG_GENERATION -> devices.generation();
            case REG_TIMEBASE -> timebase.getAsInt();
            default -> 0;
        };
    }

    private void writeRegister(final int offset, final int value) {
        switch (offset) {
            case REG_COMMAND -> run(value);
            case REG_LENGTH -> length = Math.max(0, Math.min(value, BUFFER_SIZE));
            case REG_CONTROL -> {
                control = value & (CONTROL_QUEUE | CONTROL_INTERRUPT);
                if ((control & CONTROL_QUEUE) == 0) {
                    signals.clear();
                    overflow = false;
                }
                updateInterrupt();
            }
            default -> {
            }
        }
    }

    private void run(final int command) {
        if (mainThreadCall != null) {
            return; // One call at a time.
        }
        try {
            switch (command) {
                case COMMAND_LIST -> list();
                case COMMAND_METHODS -> methods();
                case COMMAND_INVOKE -> invoke(buffer, length, false);
                case COMMAND_SIGNAL -> popSignal();
                case COMMAND_PANIC -> {
                    panic = toText(Cbor.decode(buffer, length));
                    reply(List.of());
                }
                default -> fail("unknown command");
            }
        } catch (final IllegalArgumentException e) {
            fail(e.getMessage() != null ? e.getMessage() : "malformed request");
        }
    }

    private void list() {
        final List<DeviceBus.DeviceInfo> infos = new ArrayList<>(devices.list());
        infos.sort(Comparator.comparing(info -> info.id().toString()));
        final List<Object> result = new ArrayList<>();
        for (final DeviceBus.DeviceInfo info : infos) {
            result.add(info.id().toString());
            result.add(info.typeNames().isEmpty() ? "" : info.typeNames().get(0));
        }
        reply(result);
    }

    private void methods() {
        final UUID device = parseAddress(Cbor.decode(buffer, length));
        final List<DeviceBus.MethodInfo> methods = devices.methods(device);
        if (methods == null) {
            fail("no such component");
            return;
        }
        final List<Object> result = new ArrayList<>();
        for (final DeviceBus.MethodInfo method : methods) {
            result.add(method.name());
            result.add(method.description());
        }
        reply(result);
    }

    private void invoke(final byte[] data, final int count, final boolean isMainThread) {
        final Object request;
        final UUID device;
        final String method;
        final Object[] arguments;
        try {
            request = Cbor.decode(data, count);
            if (!(request instanceof List<?> list) || list.size() < 2 || !(list.get(1) instanceof String name)) {
                throw new IllegalArgumentException("expected [address, method, args...]");
            }
            device = parseAddress(list.get(0));
            method = name;
            arguments = new Object[list.size() - 2];
            for (int i = 0; i < arguments.length; i++) {
                arguments[i] = toArgument(list.get(i + 2));
            }
        } catch (final IllegalArgumentException e) {
            fail(e.getMessage());
            return;
        }

        final Object[] results;
        try {
            results = devices.invoke(device, method, arguments, isMainThread);
        } catch (final DeviceBus.MainThreadRequired e) {
            if (isMainThread) {
                fail("internal error");
            } else {
                final byte[] copy = new byte[count];
                System.arraycopy(data, 0, copy, 0, count);
                mainThreadCall = copy;
            }
            return;
        } catch (final NoSuchMethodException e) {
            fail("no such method");
            return;
        } catch (final Exception e) {
            fail(e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());
            return;
        }
        reply(results != null ? Arrays.asList(results) : List.of());
    }

    private void popSignal() {
        final byte[] signal = signals.poll();
        overflow = false;
        updateInterrupt();
        if (signal == null) {
            length = 0;
            status = STATUS_DONE;
            return;
        }
        System.arraycopy(signal, 0, buffer, 0, signal.length);
        length = signal.length;
        status = STATUS_DONE;
    }

    private void reply(final List<?> values) {
        final byte[] data = Cbor.encodeArray(values);
        if (data.length > BUFFER_SIZE) {
            fail("result too large");
            return;
        }
        System.arraycopy(data, 0, buffer, 0, data.length);
        length = data.length;
        status = STATUS_DONE;
    }

    private void fail(final String message) {
        byte[] data = Cbor.encode(message);
        if (data.length > BUFFER_SIZE) {
            data = Cbor.encode("error");
        }
        System.arraycopy(data, 0, buffer, 0, data.length);
        length = data.length;
        status = STATUS_ERROR;
    }

    private void updateInterrupt() {
        if ((control & CONTROL_INTERRUPT) != 0 && !signals.isEmpty()) {
            interrupt.raiseInterrupt();
        } else {
            interrupt.lowerInterrupt();
        }
    }

    // --------------------------------------------------------------------- //

    private static UUID parseAddress(@Nullable final Object value) {
        if (value instanceof String text) {
            try {
                return UUID.fromString(text);
            } catch (final IllegalArgumentException ignored) {
                // Reported below.
            }
        }
        throw new IllegalArgumentException("expected a component address");
    }

    // Arrays become one-based tables, as components expect from Lua.
    @Nullable
    private static Object toArgument(@Nullable final Object value) {
        if (value instanceof List<?> list) {
            final Map<Object, Object> table = new HashMap<>();
            for (int i = 0; i < list.size(); i++) {
                table.put(i + 1, toArgument(list.get(i)));
            }
            return table;
        }
        if (value instanceof Map<?, ?> map) {
            final Map<Object, Object> table = new HashMap<>();
            for (final Map.Entry<?, ?> entry : map.entrySet()) {
                table.put(toArgument(entry.getKey()), toArgument(entry.getValue()));
            }
            return table;
        }
        return value;
    }

    private static String toText(@Nullable final Object value) {
        if (value instanceof byte[] bytes) {
            return new String(bytes, StandardCharsets.UTF_8);
        }
        return String.valueOf(value);
    }
}
