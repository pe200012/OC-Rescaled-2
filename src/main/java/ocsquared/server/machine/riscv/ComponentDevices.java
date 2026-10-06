package ocsquared.server.machine.riscv;

import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.LimitReachedException;
import li.cil.oc.api.machine.Machine;
import li.cil.oc.api.network.Component;
import li.cil.oc.api.network.Connector;
import li.cil.oc.api.network.Network;
import li.cil.oc.api.network.Node;
import ocsquared.riscv.bus.DeviceBus;

import javax.annotation.Nullable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Presents the components a machine can see as devices on the guest's device bus. Methods marked
 * direct run on the executor thread, all others are deferred to the server thread.
 * <p>
 * The machine's own computer component comes first, so that finding a computer finds it rather
 * than one next to it. It has the methods of Lua's computer API on top of its own.
 */
final class ComponentDevices implements DeviceBus.Devices {
    private static final List<DeviceBus.MethodInfo> COMPUTER_METHODS = List.of(
        new DeviceBus.MethodInfo("energy", "function():number -- The energy stored in the computer's network."),
        new DeviceBus.MethodInfo("maxEnergy", "function():number -- The energy the computer's network can store."),
        new DeviceBus.MethodInfo("uptime", "function():number -- Seconds the computer has been running, in game time."),
        new DeviceBus.MethodInfo("users", "function():table -- The players allowed to use the computer; anyone if none."),
        new DeviceBus.MethodInfo("addUser", "function(name:string):boolean -- Allows a player, who must be online, to use the computer."),
        new DeviceBus.MethodInfo("removeUser", "function(name:string):boolean -- Takes a player off the list of users."),
        new DeviceBus.MethodInfo("pushSignal", "function(name:string, ...):boolean -- Queues a signal for the computer itself.")
    );

    private final Machine machine;
    private Map<String, String> components = Map.of();
    private int generation;

    ComponentDevices(final Machine machine) {
        this.machine = machine;
    }

    /**
     * Takes a snapshot of the machine's components, bumping the generation if they changed.
     * Returns whether they did.
     */
    boolean refresh() {
        final Map<String, String> current;
        try {
            current = new HashMap<>(machine.components());
        } catch (final ConcurrentModificationException e) {
            return false; // The server thread is changing them right now, try again next time.
        }
        if (current.equals(components)) {
            return false;
        }
        components = current;
        generation++;
        return true;
    }

    // --------------------------------------------------------------------- //

    @Override
    public int generation() {
        return generation;
    }

    @Override
    public List<DeviceBus.DeviceInfo> list() {
        final String own = ownAddress();
        final List<DeviceBus.DeviceInfo> result = new ArrayList<>();
        for (final Map.Entry<String, String> component : components.entrySet()) {
            final UUID id = parseAddress(component.getKey());
            if (id != null) {
                result.add(component.getKey().equals(own) ? 0 : result.size(), new DeviceBus.DeviceInfo(id, List.of(component.getValue())));
            }
        }
        return result;
    }

    @Nullable
    @Override
    public List<DeviceBus.MethodInfo> methods(final UUID device) {
        final Component component = findComponent(device);
        if (component == null) {
            return null;
        }
        final List<DeviceBus.MethodInfo> result = new ArrayList<>();
        for (final String method : component.methods()) {
            final Callback callback = component.annotation(method);
            result.add(new DeviceBus.MethodInfo(method, callback != null ? callback.doc() : null));
        }
        if (component.address().equals(ownAddress())) {
            result.addAll(COMPUTER_METHODS);
        }
        return result;
    }

    @Override
    public Object[] invoke(final UUID device, final String method, final Object[] arguments, final boolean isMainThread) throws Exception {
        final Component component = findComponent(device);
        if (component == null) {
            throw new IllegalArgumentException("no such component");
        }
        if (component.address().equals(ownAddress()) && isComputerMethod(method)) {
            return invokeComputer(method, arguments, isMainThread);
        }
        final Callback callback = component.annotation(method);
        if (callback == null) {
            throw new NoSuchMethodException(method);
        }
        if (!isMainThread && !callback.direct()) {
            throw DeviceBus.MainThreadRequired.INSTANCE;
        }
        try {
            return machine.invoke(component.address(), method, arguments);
        } catch (final LimitReachedException e) {
            // Out of direct calls for this tick; synchronized calls are not limited.
            if (isMainThread) {
                throw e;
            }
            throw DeviceBus.MainThreadRequired.INSTANCE;
        }
    }

    // --------------------------------------------------------------------- //

    private Object[] invokeComputer(final String method, final Object[] arguments, final boolean isMainThread) throws Exception {
        final Connector connector = machine.node() instanceof Connector c ? c : null;
        final double maxEnergy = connector != null ? connector.globalBufferSize() : 0;
        switch (method) {
            case "energy":
                return new Object[]{RiscvSettings.ignorePower() || connector == null ? maxEnergy : connector.globalBuffer()};
            case "maxEnergy":
                return new Object[]{maxEnergy};
            case "uptime":
                return new Object[]{machine.upTime()};
            case "users":
                return new Object[]{machine.users()};
            case "pushSignal":
                return new Object[]{machine.signal(stringArgument(arguments, 0), Arrays.copyOfRange(arguments, Math.min(1, arguments.length), arguments.length))};
            default:
                break;
        }
        // Changing users asks the server who is online.
        if (!isMainThread) {
            throw DeviceBus.MainThreadRequired.INSTANCE;
        }
        if (method.equals("addUser")) {
            machine.addUser(stringArgument(arguments, 0));
            return new Object[]{true};
        }
        return new Object[]{machine.removeUser(stringArgument(arguments, 0))};
    }

    private static boolean isComputerMethod(final String method) {
        for (final DeviceBus.MethodInfo info : COMPUTER_METHODS) {
            if (info.name().equals(method)) {
                return true;
            }
        }
        return false;
    }

    private static String stringArgument(final Object[] arguments, final int index) {
        if (index < arguments.length && arguments[index] instanceof String s) {
            return s;
        }
        if (index < arguments.length && arguments[index] instanceof byte[] bytes) {
            return new String(bytes, StandardCharsets.UTF_8);
        }
        throw new IllegalArgumentException("bad argument #" + (index + 1) + " (string expected)");
    }

    @Nullable
    private String ownAddress() {
        final Node node = machine.node();
        return node != null ? node.address() : null;
    }

    @Nullable
    private Component findComponent(final UUID device) {
        final String address = device.toString();
        if (!components.containsKey(address)) {
            return null;
        }
        final Node node = machine.node();
        final Network network = node != null ? node.network() : null;
        if (network == null) {
            return null;
        }
        final Node target = network.node(address);
        return target instanceof Component ? (Component) target : null;
    }

    @Nullable
    static UUID parseAddress(final Object value) {
        if (!(value instanceof String)) {
            return null;
        }
        try {
            return UUID.fromString((String) value);
        } catch (final IllegalArgumentException e) {
            return null;
        }
    }
}
