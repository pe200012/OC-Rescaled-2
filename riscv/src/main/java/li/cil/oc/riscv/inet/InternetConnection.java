/* SPDX-License-Identifier: MIT */
// Adapted from OpenComputers II (https://github.com/fnuecke/oc2), Copyright (c) Florian "Sangar" Nücke.

package li.cil.oc.riscv.inet;

import li.cil.oc.riscv.inet.l2.LinkLocalLayer;
import li.cil.oc.riscv.inet.util.ThrottledLogger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.time.Duration;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public final class InternetConnection {
    public static final int FRAME_QUEUE_SIZE = 64;
    public static final int MAX_FRAME_SIZE = LinkLocalLayer.FRAME_SIZE;

    private static final Logger LOGGER = LogManager.getLogger(InternetConnection.class);
    private static final ThrottledLogger THROTTLED_LOGGER = new ThrottledLogger(LOGGER, Duration.ofMinutes(1));
    private static final int MIN_ETHERNET_FRAME_SIZE = 42; // avoid bursts of tiny packages

    // --------------------------------------------------------------------- //

    private final InternetAdapter adapter;
    private final LinkLocalLayer stack;
    private final BlockingQueue<byte[]> toDevice = new ArrayBlockingQueue<>(FRAME_QUEUE_SIZE);
    private final BlockingQueue<byte[]> toInternet = new ArrayBlockingQueue<>(FRAME_QUEUE_SIZE);
    private final ByteBuffer receiveBuffer = ByteBuffer.allocate(MAX_FRAME_SIZE);
    private final AtomicBoolean stopRequested = new AtomicBoolean();
    private final AtomicBoolean shutdownQueued = new AtomicBoolean();
    private boolean shutDown;

    // --------------------------------------------------------------------- //

    InternetConnection(final InternetAdapter adapter, final LinkLocalLayer stack) {
        this.adapter = adapter;
        this.stack = stack;
    }

    // --------------------------------------------------------------------- //

    public void stop() {
        stopRequested.set(true);
    }

    // --------------------------------------------------------------------- //

    boolean isStopped() {
        return stopRequested.get();
    }

    boolean markShutdownQueued() {
        return shutdownQueued.compareAndSet(false, true);
    }

    boolean openInbound(final SocketChannel channel, final int guestAddress, final short guestPort) {
        return !shutDown && !isStopped() && stack.openInbound(channel, guestAddress, guestPort);
    }

    void lookUpGuest(final int address) {
        if (!shutDown) {
            stack.lookUpGuest(address);
        }
    }

    void exchangeFrames(final int byteBudget, final InternetManager.Budget shared) {
        int budget = byteBudget;
        byte[] frame;
        while (budget > 0 && shared.hasRemaining() && (frame = toDevice.poll()) != null) {
            final int cost = Math.max(frame.length, MIN_ETHERNET_FRAME_SIZE);
            budget -= cost;
            shared.charge(cost);
            adapter.writeInternetFrame(frame);
        }

        budget = byteBudget;
        while (budget > 0 && shared.hasRemaining() && toInternet.remainingCapacity() > 0
            && (frame = adapter.readInternetFrame()) != null) {
            final int cost = Math.max(frame.length, MIN_ETHERNET_FRAME_SIZE);
            budget -= cost;
            shared.charge(cost);
            toInternet.add(frame); // Only this thread adds, so the capacity check above holds.
        }
    }

    void process(final int byteBudget, final InternetManager.Budget shared) {
        if (shutDown) {
            return;
        }
        try {
            stack.onTick();

            int budget = byteBudget;
            byte[] frame;
            while (budget > 0 && shared.hasRemaining() && (frame = toInternet.poll()) != null) {
                final int cost = Math.max(frame.length, MIN_ETHERNET_FRAME_SIZE);
                budget -= cost;
                shared.charge(cost);
                stack.sendEthernetFrame(ByteBuffer.wrap(frame));
            }

            budget = byteBudget;
            while (budget > 0 && shared.hasRemaining()) {
                receiveBuffer.clear();
                if (!stack.receiveEthernetFrame(receiveBuffer)) {
                    break;
                }
                final byte[] outgoing = new byte[receiveBuffer.remaining()];
                receiveBuffer.get(outgoing);
                final int cost = Math.max(outgoing.length, MIN_ETHERNET_FRAME_SIZE);
                budget -= cost;
                shared.charge(cost);
                if (!toDevice.offer(outgoing)) {
                    break;
                }
            }
        } catch (final Exception e) {
            THROTTLED_LOGGER.error("Uncaught exception processing an internet connection.", e);
        }
    }

    void shutdown() {
        if (shutDown) {
            return;
        }
        shutDown = true;
        try {
            stack.onStop();
        } catch (final Exception e) {
            LOGGER.error("Failed to close an internet connection.", e);
        }
    }
}
