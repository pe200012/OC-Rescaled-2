package li.cil.oc.server.machine.riscv;

import li.cil.oc.api.machine.Machine;
import li.cil.oc.api.machine.MachineHost;
import li.cil.oc.api.network.Component;
import li.cil.oc.api.network.Network;
import li.cil.oc.api.network.Node;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.ToIntFunction;

/**
 * Lookups that need the mod's internals, which are compiled after this package and so are not
 * visible from it. The mod installs full implementations at startup; the defaults here only see
 * the component network. All of them run on the server thread.
 */
public final class RiscvHooks {
    /**
     * The address of a screen touching the machine's host block, or null if there is none.
     */
    public static Function<Machine, String> adjacentScreen = machine -> null;

    /**
     * The addresses of the keyboards attached to a screen, given the screen's address.
     */
    public static BiFunction<Machine, String, Collection<String>> screenKeyboards = RiscvHooks::neighboringKeyboards;

    /**
     * The inventory slot of the host's own floppy drive, as computer cases and robots have, or -1
     * if it has none.
     */
    public static ToIntFunction<MachineHost> floppySlot = host -> -1;

    private RiscvHooks() {
    }

    private static Collection<String> neighboringKeyboards(final Machine machine, final String screen) {
        final Node node = nodeOf(machine, screen);
        final List<String> keyboards = new ArrayList<>();
        if (node != null) {
            for (final Node neighbor : node.neighbors()) {
                if (neighbor instanceof Component && "keyboard".equals(((Component) neighbor).name())) {
                    keyboards.add(neighbor.address());
                }
            }
        }
        return keyboards;
    }

    @Nullable
    private static Node nodeOf(final Machine machine, final String address) {
        final Node own = machine.node();
        final Network network = own != null ? own.network() : null;
        return network != null ? network.node(address) : null;
    }
}
