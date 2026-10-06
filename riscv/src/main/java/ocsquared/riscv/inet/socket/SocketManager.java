/* SPDX-License-Identifier: MIT */
// Adapted from OpenComputers II (https://github.com/fnuecke/oc2), Copyright (c) Florian "Sangar" Nücke.

package ocsquared.riscv.inet.socket;

import ocsquared.riscv.inet.l4.AbstractSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;

public final class SocketManager implements AutoCloseable {
    public static final class ReadySessions {
        private final Set<AbstractSession> toRead = new LinkedHashSet<>();
        private final Set<AbstractSession> toConnect = new LinkedHashSet<>();

        public Set<AbstractSession> toRead() {
            return toRead;
        }

        public Set<AbstractSession> toConnect() {
            return toConnect;
        }

        public void clear() {
            toRead.clear();
            toConnect.clear();
        }
    }

    // --------------------------------------------------------------------- //

    private static final Logger LOGGER = LogManager.getLogger(SocketManager.class);

    // --------------------------------------------------------------------- //

    private final Selector selector;

    // --------------------------------------------------------------------- //

    public SocketManager() throws IOException {
        selector = Selector.open();
    }

    // --------------------------------------------------------------------- //

    /**
     * Moves every ready channel into its owner's queues. Called once per tick.
     */
    public void poll() {
        try {
            selector.selectNow(key -> {
                if (key.attachment() instanceof final Listener listener) {
                    if (key.isValid() && key.isAcceptable()) {
                        accept((ServerSocketChannel) key.channel(), listener);
                    }
                    return;
                }
                final Registration registration = (Registration) key.attachment();
                if (key.isValid() && key.isConnectable()) {
                    registration.ready().toConnect().add(registration.session());
                }
                if (key.isValid() && key.isReadable()) {
                    registration.ready().toRead().add(registration.session());
                }
            });
        } catch (final IOException e) {
            LOGGER.error("Failed to poll internet sockets.", e);
        }
    }

    public void stopReading(final SelectableChannel channel) {
        final SelectionKey key = channel.keyFor(selector);
        if (key != null && key.isValid()) {
            key.interestOpsAnd(~SelectionKey.OP_READ);
        }
    }

    public DatagramChannel openDatagramChannel(final AbstractSession session, final ReadySessions ready) throws IOException {
        final DatagramChannel channel = DatagramChannel.open();
        try {
            channel.configureBlocking(false);
            channel.register(selector, SelectionKey.OP_READ, new Registration(session, ready));
        } catch (final IOException | RuntimeException e) {
            closeQuietly(channel);
            throw e;
        }
        return channel;
    }

    /**
     * Listens on the address, handing every connection made to it to the callback, unregistered.
     */
    public void listen(final InetSocketAddress address, final Consumer<SocketChannel> onAccept) throws IOException {
        final ServerSocketChannel channel = ServerSocketChannel.open();
        try {
            channel.configureBlocking(false);
            channel.bind(address);
            channel.register(selector, SelectionKey.OP_ACCEPT, new Listener(onAccept));
        } catch (final IOException | RuntimeException e) {
            closeQuietly(channel);
            throw e;
        }
    }

    /**
     * Reports when a connection handed out by {@link #listen} has something to read.
     */
    public void register(final SocketChannel channel, final AbstractSession session, final ReadySessions ready) throws IOException {
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_READ, new Registration(session, ready));
    }

    public SocketChannel openSocketChannel(final AbstractSession session, final ReadySessions ready) throws IOException {
        final SocketChannel channel = SocketChannel.open();
        try {
            channel.configureBlocking(false);
            channel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_CONNECT,
                new Registration(session, ready));
        } catch (final IOException | RuntimeException e) {
            closeQuietly(channel);
            throw e;
        }
        return channel;
    }

    @Override
    public void close() {
        // Closing the selector alone would leak every socket registered with it.
        for (final SelectionKey key : selector.keys()) {
            closeQuietly(key.channel());
        }
        try {
            selector.close();
        } catch (final IOException e) {
            LOGGER.error("Failed to close internet socket selector.", e);
        }
    }

    // --------------------------------------------------------------------- //

    private static void accept(final ServerSocketChannel server, final Listener listener) {
        try {
            SocketChannel channel;
            while ((channel = server.accept()) != null) {
                listener.onAccept().accept(channel);
            }
        } catch (final IOException e) {
            LOGGER.warn("Failed to accept a connection on {}.", server, e);
        }
    }

    private static void closeQuietly(final Channel channel) {
        try {
            channel.close();
        } catch (final IOException e) {
            // Nothing useful to do about a channel that will not close.
        }
    }

    // --------------------------------------------------------------------- //

    private record Registration(AbstractSession session, ReadySessions ready) {
    }

    private record Listener(Consumer<SocketChannel> onAccept) {
    }
}
