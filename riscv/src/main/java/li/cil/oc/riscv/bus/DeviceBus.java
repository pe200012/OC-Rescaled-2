package li.cil.oc.riscv.bus;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import li.cil.ceres.BinarySerialization;
import li.cil.sedna.api.device.serial.SerialDevice;

import javax.annotation.Nullable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Host side of the device bus the guest's {@code devices} libraries talk to, speaking the wire
 * format of OpenComputers II: NUL-framed JSON on an RPC port, binary payloads on a blob port and
 * events on an event port. Which devices exist and what calling them does is up to {@link Devices}.
 */
public final class DeviceBus {
    public static final int MAX_PAYLOAD_SIZE = 512 * 1024;
    private static final int MAX_MESSAGE_SIZE = 4 * 1024;

    static final String ERROR_MESSAGE_TOO_LARGE = "message too large";
    static final String ERROR_UNKNOWN_MESSAGE_TYPE = "unknown message type";
    static final String ERROR_UNKNOWN_DEVICE = "unknown device";
    static final String ERROR_UNKNOWN_METHOD = "unknown method";
    static final String ERROR_PAYLOAD_TOO_LARGE = "payload is larger than the channel allows";
    static final String ERROR_PAYLOAD_CORRUPT = "payload failed its checksum";
    static final String ERROR_PAYLOAD_MISMATCH = "payload does not match its description";
    static final String ERROR_MALFORMED_MESSAGE = "malformed message";
    static final String ERROR_INTERNAL = "internal error";
    static final String ERROR_PAYLOAD_NEEDS_UNSYNCHRONIZED = "binary parameters require a method that is not synchronized";

    private static final String MESSAGE_TYPE_LIST = "list";
    private static final String MESSAGE_TYPE_METHODS = "methods";
    private static final String MESSAGE_TYPE_INVOKE = "invoke";
    private static final String MESSAGE_TYPE_RESULT = "result";
    private static final String MESSAGE_TYPE_ERROR = "error";
    private static final String MESSAGE_TYPE_DEVICES_CHANGED = "devicesChanged";
    private static final String MESSAGE_TYPE_EVENTS_DROPPED = "eventsDropped";

    private static final String BLOB_REFERENCE_KEY = "$blob";

    // --------------------------------------------------------------------- //

    public interface Devices {
        /**
         * Changes whenever the set of devices changes.
         */
        int generation();

        List<DeviceInfo> list();

        /**
         * The methods of a device, or null if there is no such device.
         */
        @Nullable
        List<MethodInfo> methods(UUID device);

        /**
         * Calls a method. Throws {@link MainThreadRequired} if it may only run on the main thread
         * and {@code isMainThread} is false; the call is then repeated from {@link #runMainThreadCall()}.
         */
        Object[] invoke(UUID device, String method, Object[] arguments, boolean isMainThread) throws Exception;
    }

    public record DeviceInfo(UUID id, List<String> typeNames) {
    }

    public record MethodInfo(String name, @Nullable String description) {
    }

    public static final class MainThreadRequired extends RuntimeException {
        public static final MainThreadRequired INSTANCE = new MainThreadRequired();

        private MainThreadRequired() {
            super(null, null, false, false);
        }
    }

    private record Invocation(int requestId, UUID device, String method, JsonArray parameters) {
    }

    // --------------------------------------------------------------------- //

    private final Devices devices;
    private final RPCMessageChannel messages;
    private final RPCPayloadChannel payloads;
    private final RPCEventChannel events;
    private int generation;

    private int currentRequestId;
    private byte[] receivedBlob;
    private byte[] pendingBlob;
    private volatile Invocation mainThreadCall;

    public DeviceBus(final Devices devices, final SerialDevice rpcPort, final SerialDevice blobPort, final SerialDevice eventPort) {
        this.devices = devices;
        this.messages = new RPCMessageChannel(rpcPort, MAX_MESSAGE_SIZE);
        this.payloads = new RPCPayloadChannel(blobPort);
        this.events = new RPCEventChannel(eventPort);
        this.generation = devices.generation();
    }

    // --------------------------------------------------------------------- //

    /**
     * Exchanges data with the guest. Called from the machine's executor thread.
     */
    public void step() {
        final int currentGeneration = devices.generation();
        if (currentGeneration != generation) {
            generation = currentGeneration;
            sendEvent(null, MESSAGE_TYPE_DEVICES_CHANGED, JsonNull.INSTANCE);
        }

        payloads.receive();
        while (!messages.isSending() && !payloads.isSending() && mainThreadCall == null) {
            currentRequestId = 0;
            if (!messages.readFrame(this::acceptMessage, this::rejectOversizedMessage)) {
                break;
            }
        }
        payloads.flush();
        messages.flush();

        final int dropped = events.takeDropped();
        if (dropped > 0) {
            events.sendNotice(RPCMessageChannel.frame(encode(envelope(MESSAGE_TYPE_EVENTS_DROPPED, 0, null, new JsonPrimitive(dropped)))));
        }
        events.flush();
    }

    public boolean hasMainThreadCall() {
        return mainThreadCall != null;
    }

    /**
     * Runs the call that asked for the main thread. Called from the server thread.
     */
    public void runMainThreadCall() {
        final Invocation invocation = mainThreadCall;
        if (invocation == null) {
            return;
        }
        currentRequestId = invocation.requestId();
        invoke(invocation, true);
        // Cleared last, so the executor thread does not read the next message before the reply is out.
        mainThreadCall = null;
    }

    public void saveState(final DataOutputStream output) throws IOException {
        BinarySerialization.serialize(output, messages, RPCMessageChannel.class);
        BinarySerialization.serialize(output, payloads, RPCPayloadChannel.class);
        BinarySerialization.serialize(output, events, RPCEventChannel.class);
        output.writeInt(generation);

        // A call waiting for the main thread when the game saves must still get its reply later.
        final Invocation invocation = mainThreadCall;
        output.writeBoolean(invocation != null);
        if (invocation != null) {
            output.writeInt(invocation.requestId());
            output.writeUTF(invocation.device().toString());
            output.writeUTF(invocation.method());
            output.writeUTF(invocation.parameters().toString());
        }
    }

    public void loadState(final DataInputStream input) throws IOException {
        BinarySerialization.deserialize(input, RPCMessageChannel.class, messages);
        BinarySerialization.deserialize(input, RPCPayloadChannel.class, payloads);
        BinarySerialization.deserialize(input, RPCEventChannel.class, events);
        generation = input.readInt();

        mainThreadCall = input.readBoolean()
            ? new Invocation(input.readInt(), UUID.fromString(input.readUTF()), input.readUTF(),
                JsonParser.parseString(input.readUTF()).getAsJsonArray())
            : null;
    }

    /**
     * Queues an event for the guest; returns false if the queue is full and it was dropped.
     */
    public boolean sendEvent(@Nullable final UUID device, final String type, final Object[] data) {
        final JsonArray values = new JsonArray();
        for (final Object value : data) {
            values.add(toJson(value, false));
        }
        return sendEvent(device, type, values);
    }

    private boolean sendEvent(@Nullable final UUID device, final String type, final JsonElement data) {
        final JsonObject message = envelope(type, 0, device, data);
        return events.sendEvent(RPCMessageChannel.frame(encode(message)));
    }

    // --------------------------------------------------------------------- //

    private void rejectOversizedMessage() {
        payloads.discard();
        writeError(ERROR_MESSAGE_TOO_LARGE);
    }

    private void acceptMessage(final byte[] data) {
        final JsonObject message;
        try {
            message = JsonParser.parseString(new String(data, StandardCharsets.UTF_8)).getAsJsonObject();
        } catch (final RuntimeException e) {
            payloads.discard();
            writeError(ERROR_MALFORMED_MESSAGE);
            return;
        }

        currentRequestId = message.has("id") ? message.get("id").getAsInt() : 0;

        final JsonElement blob = message.get("blob");
        if (blob != null && blob.isJsonObject()) {
            try {
                final JsonObject reference = blob.getAsJsonObject();
                receivedBlob = payloads.take(reference.get("length").getAsInt(), reference.get("checksum").getAsInt());
            } catch (final RPCPayloadChannel.PayloadException e) {
                writeError(e.error);
                return;
            } catch (final RuntimeException e) {
                payloads.discard();
                writeError(ERROR_MALFORMED_MESSAGE);
                return;
            }
        } else {
            payloads.discard();
        }

        try {
            processMessage(message);
        } catch (final RuntimeException e) {
            writeError(ERROR_MALFORMED_MESSAGE);
        } finally {
            receivedBlob = null;
        }
    }

    private void processMessage(final JsonObject message) {
        switch (message.get("type").getAsString()) {
            case MESSAGE_TYPE_LIST -> writeDeviceList();
            case MESSAGE_TYPE_METHODS -> writeMethods(UUID.fromString(message.get("data").getAsString()));
            case MESSAGE_TYPE_INVOKE -> {
                final JsonObject data = message.getAsJsonObject("data");
                final JsonElement parameters = data.get("parameters");
                invoke(new Invocation(
                    currentRequestId,
                    UUID.fromString(data.get("deviceId").getAsString()),
                    data.get("name").getAsString(),
                    parameters != null && parameters.isJsonArray() ? parameters.getAsJsonArray() : new JsonArray()), false);
            }
            default -> writeError(ERROR_UNKNOWN_MESSAGE_TYPE);
        }
    }

    private void writeDeviceList() {
        final JsonArray list = new JsonArray();
        for (final DeviceInfo device : devices.list()) {
            final JsonObject entry = new JsonObject();
            entry.addProperty("deviceId", device.id().toString());
            final JsonArray typeNames = new JsonArray();
            device.typeNames().forEach(typeNames::add);
            entry.add("typeNames", typeNames);
            list.add(entry);
        }
        writeMessage(MESSAGE_TYPE_LIST, list);
    }

    private void writeMethods(final UUID device) {
        final List<MethodInfo> methods = devices.methods(device);
        if (methods == null) {
            writeError(ERROR_UNKNOWN_DEVICE);
            return;
        }

        // Component methods take whatever arguments they are given, so all are listed as variadic.
        final JsonArray list = new JsonArray();
        for (final MethodInfo method : methods) {
            final JsonObject entry = new JsonObject();
            entry.addProperty("name", method.name());
            if (method.description() != null && !method.description().isEmpty()) {
                entry.addProperty("description", method.description());
            }
            final JsonArray parameters = new JsonArray();
            final JsonObject variadic = new JsonObject();
            variadic.addProperty("name", "...");
            parameters.add(variadic);
            entry.add("parameters", parameters);
            list.add(entry);
        }
        writeMessage(MESSAGE_TYPE_METHODS, list);
    }

    private void invoke(final Invocation invocation, final boolean isMainThread) {
        final Object[] arguments;
        try {
            arguments = new Object[invocation.parameters().size()];
            for (int i = 0; i < arguments.length; i++) {
                arguments[i] = fromJson(invocation.parameters().get(i));
            }
        } catch (final IllegalArgumentException e) {
            writeError(e.getMessage());
            return;
        }

        final Object[] results;
        try {
            results = devices.invoke(invocation.device(), invocation.method(), arguments, isMainThread);
        } catch (final MainThreadRequired e) {
            if (isMainThread || receivedBlob != null) {
                writeError(isMainThread ? ERROR_INTERNAL : ERROR_PAYLOAD_NEEDS_UNSYNCHRONIZED);
            } else {
                mainThreadCall = invocation;
            }
            return;
        } catch (final NoSuchMethodException e) {
            writeError(ERROR_UNKNOWN_METHOD);
            return;
        } catch (final Exception e) {
            final String message = e.getMessage();
            writeError(message != null ? message : e.getClass().getSimpleName());
            return;
        }

        final JsonElement data;
        if (results == null || results.length == 0) {
            data = JsonNull.INSTANCE;
        } else if (results.length == 1) {
            data = toJson(results[0], true);
        } else {
            final JsonArray array = new JsonArray();
            for (final Object result : results) {
                array.add(toJson(result, true));
            }
            data = array;
        }
        writeMessage(MESSAGE_TYPE_RESULT, data);
    }

    private void writeError(final String message) {
        pendingBlob = null;
        writeMessage(MESSAGE_TYPE_ERROR, new JsonPrimitive(message));
    }

    private void writeMessage(final String type, final JsonElement data) {
        final byte[] blob = pendingBlob;
        pendingBlob = null;
        if (blob != null && blob.length > MAX_PAYLOAD_SIZE) {
            writeError(ERROR_PAYLOAD_TOO_LARGE);
            return;
        }

        final JsonObject message = envelope(type, currentRequestId, null, data);
        if (blob != null) {
            final JsonObject reference = new JsonObject();
            reference.addProperty("length", blob.length);
            reference.addProperty("checksum", RPCPayloadChannel.checksum(blob));
            message.add("blob", reference);
            payloads.send(blob);
        }
        messages.send(RPCMessageChannel.frame(encode(message)));
    }

    private JsonObject envelope(final String type, final int id, @Nullable final UUID device, final JsonElement data) {
        final JsonObject message = new JsonObject();
        message.addProperty("type", type);
        message.addProperty("id", id);
        message.addProperty("gen", generation);
        if (device != null) {
            message.addProperty("deviceId", device.toString());
        }
        message.add("data", data);
        return message;
    }

    private static byte[] encode(final JsonObject message) {
        return message.toString().getBytes(StandardCharsets.UTF_8);
    }

    // --------------------------------------------------------------------- //

    /**
     * JSON to the values components expect: numbers as doubles, arrays as one-based tables.
     */
    @Nullable
    private Object fromJson(final JsonElement json) {
        if (json == null || json.isJsonNull()) {
            return null;
        }
        if (json.isJsonPrimitive()) {
            final JsonPrimitive primitive = json.getAsJsonPrimitive();
            if (primitive.isBoolean()) {
                return primitive.getAsBoolean();
            }
            if (primitive.isNumber()) {
                return primitive.getAsDouble();
            }
            return primitive.getAsString();
        }
        if (json.isJsonArray()) {
            final Map<Object, Object> table = new HashMap<>();
            int index = 1;
            for (final JsonElement element : json.getAsJsonArray()) {
                table.put(index++, fromJson(element));
            }
            return table;
        }

        final JsonObject object = json.getAsJsonObject();
        if (object.has(BLOB_REFERENCE_KEY)) {
            if (receivedBlob == null) {
                throw new IllegalArgumentException("parameter refers to a payload that was not sent");
            }
            return receivedBlob;
        }
        final Map<Object, Object> table = new HashMap<>();
        for (final Map.Entry<String, JsonElement> entry : object.entrySet()) {
            table.put(entry.getKey(), fromJson(entry.getValue()));
        }
        return table;
    }

    /**
     * Component results to JSON. Binary strings go out as a payload where allowed.
     */
    private JsonElement toJson(@Nullable final Object value, final boolean allowBlob) {
        if (value == null) {
            return JsonNull.INSTANCE;
        }
        if (value instanceof Boolean b) {
            return new JsonPrimitive(b);
        }
        if (value instanceof Character c) {
            return new JsonPrimitive((int) c);
        }
        if (value instanceof Number n) {
            return new JsonPrimitive(n);
        }
        if (value instanceof String s) {
            return new JsonPrimitive(s);
        }
        if (value instanceof byte[] bytes) {
            final String text = decodeUtf8(bytes);
            if (text != null || !allowBlob || pendingBlob != null) {
                return new JsonPrimitive(text != null ? text : new String(bytes, StandardCharsets.ISO_8859_1));
            }
            pendingBlob = bytes;
            final JsonObject marker = new JsonObject();
            marker.addProperty(BLOB_REFERENCE_KEY, true);
            return marker;
        }
        if (value instanceof Map<?, ?> map) {
            return mapToJson(map, allowBlob);
        }
        if (value instanceof Object[] array) {
            final JsonArray result = new JsonArray();
            for (final Object element : array) {
                result.add(toJson(element, allowBlob));
            }
            return result;
        }
        if (value instanceof Collection<?> collection) {
            final JsonArray result = new JsonArray();
            for (final Object element : collection) {
                result.add(toJson(element, allowBlob));
            }
            return result;
        }
        return new JsonPrimitive(value.toString());
    }

    private JsonElement mapToJson(final Map<?, ?> map, final boolean allowBlob) {
        // A table with keys 1..n is a list.
        final List<Object> sequence = new ArrayList<>();
        for (int i = 1; i <= map.size(); i++) {
            final Object key = map.containsKey(i) ? (Object) i : map.containsKey((double) i) ? (Object) (double) i : null;
            if (key == null) {
                sequence.clear();
                break;
            }
            sequence.add(map.get(key));
        }
        if (!map.isEmpty() && sequence.size() == map.size()) {
            final JsonArray result = new JsonArray();
            for (final Object element : sequence) {
                result.add(toJson(element, allowBlob));
            }
            return result;
        }

        final JsonObject result = new JsonObject();
        for (final Map.Entry<?, ?> entry : map.entrySet()) {
            final Object key = entry.getKey();
            final String name = key instanceof Double d && d == Math.rint(d) ? String.valueOf(d.longValue()) : String.valueOf(key);
            result.add(name, toJson(entry.getValue(), allowBlob));
        }
        return result;
    }

    @Nullable
    private static String decodeUtf8(final byte[] bytes) {
        try {
            return StandardCharsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT)
                .decode(ByteBuffer.wrap(bytes))
                .toString();
        } catch (final CharacterCodingException e) {
            return null;
        }
    }
}
