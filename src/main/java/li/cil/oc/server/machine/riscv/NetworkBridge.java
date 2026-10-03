package li.cil.oc.server.machine.riscv;

import li.cil.oc.api.machine.Machine;
import li.cil.oc.riscv.RiscvMachine;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Connects the machine's network interfaces to its network cards: each Ethernet frame travels as
 * one network message on a reserved port, so it crosses cables, switches, relays and wireless
 * links like any other message. Unicast frames go to the card their destination was last heard
 * from, all others are broadcast.
 */
final class NetworkBridge {
    private static final Logger LOGGER = LogManager.getLogger("OpenComputers/RISC-V");

    // Ports are free to pick; this one is the EtherType of IPv4.
    static final int PORT = 0x0800;
    private static final int MAX_QUEUED_FRAMES = 256;
    private static final int MAX_LEARNED_ADDRESSES = 256;
    private static final int ETHERNET_HEADER_SIZE = 14;

    private record Outgoing(int network, byte[] frame) {
    }

    private final Machine machine;
    private final List<String> cards;
    private final ArrayDeque<Outgoing> outgoing = new ArrayDeque<>();
    private final Map<Long, String> learned = new LinkedHashMap<>(16, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(final Map.Entry<Long, String> eldest) {
            return size() > MAX_LEARNED_ADDRESSES;
        }
    };
    private boolean arePortsOpen;

    /**
     * @param cards the network cards' addresses; the card at index i serves interface i.
     */
    NetworkBridge(final Machine machine, final List<String> cards) {
        this.machine = machine;
        this.cards = List.copyOf(cards);
    }

    /**
     * The network cards the machine can see, in a stable order. Runs on the server thread.
     */
    static List<String> findCards(final Machine machine) {
        return findComponents(machine, "modem");
    }

    /**
     * Addresses of the components of a type the machine can see, sorted. Runs on the server thread.
     */
    static List<String> findComponents(final Machine machine, final String name) {
        final List<String> addresses = new ArrayList<>();
        for (final Map.Entry<String, String> component : new HashMap<>(machine.components()).entrySet()) {
            if (name.equals(component.getValue())) {
                addresses.add(component.getKey());
            }
        }
        addresses.sort(null);
        return addresses;
    }

    int networkCount() {
        return cards.size();
    }

    // --------------------------------------------------------------------- //
    // Executor thread.

    /**
     * Takes the frames the guest sent, to be passed on by {@link #flush()}.
     */
    void collect(final RiscvMachine vm) {
        for (int network = 0; network < cards.size(); network++) {
            byte[] frame;
            while ((frame = vm.readFrame(network)) != null) {
                if (outgoing.size() < MAX_QUEUED_FRAMES && frame.length >= ETHERNET_HEADER_SIZE) {
                    outgoing.add(new Outgoing(network, frame));
                }
            }
        }
    }

    boolean needsServerThread() {
        return !arePortsOpen || !outgoing.isEmpty();
    }

    /**
     * Hands a received frame to the guest. Returns false if the signal is not a frame for us.
     * Arguments of the modem_message signal: receiving card, sender, port, distance, data.
     */
    boolean accept(final RiscvMachine vm, final Object[] args) {
        if (args.length < 5 || !(args[2] instanceof Number port) || port.intValue() != PORT
            || !(args[1] instanceof String sender) || !(args[4] instanceof byte[] frame)) {
            return false;
        }
        final int network = cards.indexOf(String.valueOf(args[0]));
        if (network < 0) {
            return false;
        }
        if (frame.length >= ETHERNET_HEADER_SIZE) {
            learned.put(macAt(frame, 6), sender);
            vm.writeFrame(network, frame);
        }
        return true;
    }

    // --------------------------------------------------------------------- //
    // Server thread.

    /**
     * Opens the port on all cards the first time, then sends the collected frames.
     */
    void flush() {
        if (!arePortsOpen) {
            arePortsOpen = true;
            for (final String card : cards) {
                try {
                    machine.invoke(card, "open", new Object[]{PORT});
                } catch (final Exception e) {
                    LOGGER.warn("Failed opening port {} on network card {}: {}", PORT, card, e.getMessage());
                }
            }
        }

        Outgoing entry;
        while ((entry = outgoing.poll()) != null) {
            final String card = cards.get(entry.network());
            final long destination = macAt(entry.frame(), 0);
            final boolean isGroup = (entry.frame()[0] & 1) != 0;
            final String receiver = isGroup ? null : learned.get(destination);
            try {
                if (receiver != null) {
                    machine.invoke(card, "send", new Object[]{receiver, PORT, entry.frame()});
                } else {
                    machine.invoke(card, "broadcast", new Object[]{PORT, entry.frame()});
                }
            } catch (final Exception e) {
                // Card removed or out of energy; the guest sees it as a lost frame.
                LOGGER.debug("Failed sending frame on network card {}.", card, e);
            }
        }
    }

    // --------------------------------------------------------------------- //

    private static long macAt(final byte[] frame, final int offset) {
        long mac = 0;
        for (int i = 0; i < 6; i++) {
            mac = (mac << 8) | (frame[offset + i] & 0xFF);
        }
        return mac;
    }
}
