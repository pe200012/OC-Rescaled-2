package li.cil.oc.server.machine.riscv;

import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.LimitReachedException;
import li.cil.oc.api.machine.Machine;
import li.cil.oc.api.network.Component;
import li.cil.oc.api.network.Network;
import li.cil.oc.api.network.Node;
import li.cil.oc.riscv.bus.DeviceBus;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Presents the components a machine can see as devices on the guest's device bus. Methods marked
 * direct run on the executor thread, all others are deferred to the server thread.
 */
final class ComponentDevices implements DeviceBus.Devices {
    private final Machine machine;
    private Map<String, String> components = Map.of();
    private int generation;

    ComponentDevices(final Machine machine) {
        this.machine = machine;
    }

    /**
     * Takes a snapshot of the machine's components, bumping the generation if they changed.
     */
    void refresh() {
        final Map<String, String> current;
        try {
            current = new HashMap<>(machine.components());
        } catch (final ConcurrentModificationException e) {
            return; // The server thread is changing them right now, try again next time.
        }
        if (!current.equals(components)) {
            components = current;
            generation++;
        }
    }

    // --------------------------------------------------------------------- //

    @Override
    public int generation() {
        return generation;
    }

    @Override
    public List<DeviceBus.DeviceInfo> list() {
        final List<DeviceBus.DeviceInfo> result = new ArrayList<>();
        for (final Map.Entry<String, String> component : components.entrySet()) {
            final UUID id = parseAddress(component.getKey());
            if (id != null) {
                result.add(new DeviceBus.DeviceInfo(id, List.of(component.getValue())));
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
        return result;
    }

    @Override
    public Object[] invoke(final UUID device, final String method, final Object[] arguments, final boolean isMainThread) throws Exception {
        final Component component = findComponent(device);
        if (component == null) {
            throw new IllegalArgumentException("no such component");
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
