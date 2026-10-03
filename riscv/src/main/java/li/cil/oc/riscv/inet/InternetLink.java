package li.cil.oc.riscv.inet;

import li.cil.oc.riscv.RiscvMachine;

import javax.annotation.Nullable;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Connects one of a machine's network interfaces to the internet. Frames are handed over through
 * queues, as the machine runs on its executor thread and the {@link InternetManager} on the server
 * thread. The far end behaves like a router at whatever address the guest routes through.
 */
public final class InternetLink implements InternetAdapter {
    private final int network;
    private final String origin;
    private final BlockingQueue<byte[]> fromGuest = new ArrayBlockingQueue<>(InternetConnection.FRAME_QUEUE_SIZE);
    private final BlockingQueue<byte[]> toGuest = new ArrayBlockingQueue<>(InternetConnection.FRAME_QUEUE_SIZE);
    @Nullable
    private volatile InternetConnection connection;

    /**
     * @param network the machine's network interface this link serves.
     * @param origin  who the traffic is from, for the log.
     */
    public InternetLink(final int network, final String origin) {
        this.network = network;
        this.origin = origin;
    }

    public synchronized void disconnect() {
        final InternetConnection current = connection;
        connection = null;
        if (current != null) {
            current.stop();
        }
        fromGuest.clear();
        toGuest.clear();
    }

    /**
     * Moves frames between the machine and the queues. Called from the machine's thread. Connects
     * on first use, as machines may start before internet access does.
     */
    public void exchange(final RiscvMachine machine) {
        final InternetConnection current = connection != null ? connection : connect();
        byte[] frame;
        while ((frame = machine.readFrame(network)) != null) {
            if (current != null && frame.length <= InternetConnection.MAX_FRAME_SIZE) {
                fromGuest.offer(frame); // Dropped when full, like on a congested link.
            }
        }
        while ((frame = toGuest.poll()) != null) {
            machine.writeFrame(network, frame);
        }
    }

    @Nullable
    private synchronized InternetConnection connect() {
        if (connection == null) {
            connection = InternetManager.getInstance().map(manager -> manager.connect(this, origin)).orElse(null);
        }
        return connection;
    }

    // --------------------------------------------------------------------- //
    // Called by the internet manager on the server thread.

    @Nullable
    @Override
    public byte[] readInternetFrame() {
        return fromGuest.poll();
    }

    @Override
    public void writeInternetFrame(final byte[] frame) {
        toGuest.offer(frame);
    }
}
