/* SPDX-License-Identifier: MIT */
// Adapted from OpenComputers II (https://github.com/fnuecke/oc2), Copyright (c) Florian "Sangar" Nücke.

package li.cil.oc.riscv.inet;

import li.cil.oc.riscv.inet.l2.LinkLocalLayer;
import li.cil.oc.riscv.inet.l3.AddressFilter;
import li.cil.oc.riscv.inet.l3.NetworkLayer;
import li.cil.oc.riscv.inet.l4.*;
import li.cil.oc.riscv.inet.socket.ReachabilityProbe;
import li.cil.oc.riscv.inet.socket.SocketManager;
import li.cil.oc.riscv.inet.socket.SocketSessionLayer;
import li.cil.oc.riscv.inet.util.AddressParseException;
import li.cil.oc.riscv.inet.util.InternetUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public final class InternetManager {
    private static final int SESSIONS_PER_GATEWAY = 16;
    private static final double NEW_SESSIONS_PER_SECOND = 4;
    private static final int HOST_REFRESH_SECONDS = 300;
    private static final int SESSION_TIMEOUT_MS = 60 * 1000;
    private static final int ECHO_TIMEOUT_MS = 1000;
    private static final int ECHO_THREADS = 4;
    private static final long FORWARD_TIMEOUT_NANOS = TimeUnit.SECONDS.toNanos(5);

    private static final Logger LOGGER = LogManager.getLogger(InternetManager.class);

    // --------------------------------------------------------------------- //

    @Nullable
    private static InternetManager instance;

    private final AddressFilter addressFilter;
    private final PortFilter portFilter;

    private final SessionLimits limits;
    private final SocketManager socketManager;
    private final ExecutorService internetThread;
    private final ExecutorService echoExecutor;
    private final ReachabilityProbe reachabilityProbe;

    @Nullable
    private final ScheduledExecutorService maintenanceExecutor;
    private final List<InternetConnection> connections = new CopyOnWriteArrayList<>();
    private final List<PendingForward> pendingForwards = new ArrayList<>(); // Internet thread only.
    private final Queue<Runnable> commands = new ConcurrentLinkedQueue<>();
    private final AtomicBoolean tickInFlight = new AtomicBoolean();
    private final AtomicBoolean stopped = new AtomicBoolean();
    private final int bytesPerTick;
    private final int sharedBytesPerTick;
    private int serverRoundRobin;
    private int workerRoundRobin;

    // --------------------------------------------------------------------- //

    private InternetManager() throws IOException {
        addressFilter = AddressFilter.withBuiltInDenials(
            InternetConfig.internetAllowedHosts, InternetConfig.internetDeniedHosts, InternetConfig.internetDenyLocalSubnets);
        portFilter = new PortFilter(InternetConfig.internetDeniedPorts);
        limits = new SessionLimits(SESSIONS_PER_GATEWAY, InternetConfig.internetSessionsTotal);
        socketManager = new SocketManager();

        internetThread = Executors.newSingleThreadExecutor(runnable -> daemon(runnable, "OC RISC-V Internet"));
        echoExecutor = new ThreadPoolExecutor(0, ECHO_THREADS,
            30, TimeUnit.SECONDS, new SynchronousQueue<>(),
            runnable -> daemon(runnable, "OC RISC-V Internet Probe"));
        reachabilityProbe = new ReachabilityProbe(echoExecutor, ECHO_TIMEOUT_MS);

        final int ticksPerSecond = 20;
        bytesPerTick = Math.max(LinkLocalLayer.FRAME_SIZE, InternetConfig.internetBytesPerSecond / ticksPerSecond);
        sharedBytesPerTick = Math.max(LinkLocalLayer.FRAME_SIZE,
            InternetConfig.internetBytesPerSecondTotal / ticksPerSecond);

        if (addressFilter.needsPeriodicRefresh()) {
            maintenanceExecutor = Executors.newSingleThreadScheduledExecutor(
                runnable -> daemon(runnable, "OC RISC-V Internet Filter"));
            maintenanceExecutor.scheduleWithFixedDelay(this::refreshFilter,
                HOST_REFRESH_SECONDS, HOST_REFRESH_SECONDS, TimeUnit.SECONDS);
        } else {
            maintenanceExecutor = null;
        }

        final List<Forward> forwards = parseForwards(InternetConfig.internetForwards);
        commands.add(() -> listen(forwards));
    }

    // --------------------------------------------------------------------- //

    public static void start() {
        if (instance != null) {
            LOGGER.warn("Internet manager already running.");
            return;
        }
        if (!InternetConfig.internetEnabled) {
            LOGGER.info("Internet access is disabled.");
            return;
        }
        try {
            instance = new InternetManager();
        } catch (final IOException e) {
            LOGGER.error("Failed to start internet manager; internet access is unavailable.", e);
            return;
        }
        LOGGER.info("Internet access is enabled. Computers can reach the network this server sits on, "
            + "subject to the configured address filter: {}", instance.addressFilter);
    }

    public static void stop() {
        final InternetManager manager = instance;
        instance = null;
        if (manager != null) {
            manager.shutdown();
        }
    }

    public static Optional<InternetManager> getInstance() {
        return Optional.ofNullable(instance);
    }

    public InternetConnection connect(final InternetAdapter adapter, final String originDescription) {
        final InternetConnection connection =
            new InternetConnection(adapter, buildStack(originDescription));
        connections.add(connection);
        return connection;
    }

    public void onServerTick() {
        final Budget shared = new Budget(sharedBytesPerTick);
        for (final InternetConnection connection : inRotation(serverRoundRobin++)) {
            if (connection.isStopped()) {
                if (connection.markShutdownQueued()) {
                    commands.add(connection::shutdown);
                }
            } else {
                connection.exchangeFrames(bytesPerTick, shared);
            }
        }
        connections.removeIf(InternetConnection::isStopped);

        if (!tickInFlight.compareAndSet(false, true)) {
            // The internet thread has not finished the previous pass. Don't
            // grow the executor queue.
            return;
        }
        try {
            internetThread.execute(() -> {
                try {
                    runWorkerTick();
                } finally {
                    tickInFlight.set(false);
                }
            });
        } catch (final RuntimeException e) {
            tickInFlight.set(false);
            LOGGER.error("Failed to schedule internet thread work.", e);
        }
    }

    // --------------------------------------------------------------------- //

    private record Forward(int hostPort, int guestAddress, short guestPort) {
        @Override
        public String toString() {
            final StringBuilder builder = new StringBuilder();
            InternetUtils.socketAddressToString(builder, guestAddress, guestPort);
            return builder.toString();
        }
    }

    private record PendingForward(SocketChannel channel, Forward forward, long deadline) {
    }

    static final class Budget {
        private int remaining;

        Budget(final int remaining) {
            this.remaining = remaining;
        }

        boolean hasRemaining() {
            return remaining > 0;
        }

        void charge(final int bytes) {
            remaining -= bytes;
        }
    }

    private static Thread daemon(final Runnable runnable, final String name) {
        final Thread thread = new Thread(runnable, name);
        thread.setDaemon(true);
        return thread;
    }

    private LinkLocalLayer buildStack(final String originDescription) {
        final SessionLayer sessionLayer = new SocketSessionLayer(originDescription, socketManager, reachabilityProbe);
        final TransportLayer transportLayer = new TransportLayer(sessionLayer, portFilter, limits,
            new TokenBucket(SESSIONS_PER_GATEWAY, NEW_SESSIONS_PER_SECOND),
            connections::size,
            StreamSession.TcpConfig.DEFAULT,
            TimeUnit.MILLISECONDS.toNanos(SESSION_TIMEOUT_MS));
        final NetworkLayer networkLayer = new NetworkLayer(transportLayer, addressFilter);
        return new LinkLocalLayer(networkLayer);
    }

    private void runWorkerTick() {
        try {
            socketManager.poll();
            runCommands();
            connectForwards();

            final Budget shared = new Budget(sharedBytesPerTick);
            for (final InternetConnection connection : inRotation(workerRoundRobin++)) {
                connection.process(bytesPerTick, shared);
            }
        } catch (final Throwable t) {
            LOGGER.error("Uncaught error on the internet thread.", t);
        }
    }

    private List<InternetConnection> inRotation(final int rotation) {
        final List<InternetConnection> snapshot = List.copyOf(connections);
        if (snapshot.size() < 2) {
            return snapshot;
        }
        final int offset = Math.floorMod(rotation, snapshot.size());
        final List<InternetConnection> rotated = new ArrayList<>(snapshot.size());
        rotated.addAll(snapshot.subList(offset, snapshot.size()));
        rotated.addAll(snapshot.subList(0, offset));
        return rotated;
    }

    private void runCommands() {
        Runnable command;
        while ((command = commands.poll()) != null) {
            try {
                command.run();
            } catch (final Exception e) {
                LOGGER.error("Uncaught exception running internet thread command.", e);
            }
        }
    }

    private static List<Forward> parseForwards(final List<String> entries) {
        final List<Forward> forwards = new ArrayList<>();
        for (final String entry : entries) {
            final String[] parts = entry.trim().split(":");
            try {
                if (parts.length != 3) {
                    throw new IllegalArgumentException();
                }
                forwards.add(new Forward(parsePort(parts[0]), InternetUtils.parseIpv4Address(parts[1]), (short) parsePort(parts[2])));
            } catch (final AddressParseException | IllegalArgumentException e) {
                LOGGER.error("Ignoring port forward '{}'; expected hostPort:guestAddress:guestPort, like 2222:10.0.2.15:22.", entry);
            }
        }
        return forwards;
    }

    private static int parsePort(final String string) {
        final int port = Integer.parseInt(string);
        if (port < 1 || port > 0xFFFF) {
            throw new IllegalArgumentException();
        }
        return port;
    }

    private void listen(final List<Forward> forwards) {
        for (final Forward forward : forwards) {
            final InetSocketAddress address = new InetSocketAddress(InetAddress.getLoopbackAddress(), forward.hostPort());
            try {
                socketManager.listen(address, channel -> {
                    // Ask every link at once; the guest holding the address answers in a tick or two.
                    for (final InternetConnection connection : connections) {
                        connection.lookUpGuest(forward.guestAddress());
                    }
                    pendingForwards.add(new PendingForward(channel, forward, System.nanoTime() + FORWARD_TIMEOUT_NANOS));
                });
                LOGGER.info("Forwarding connections to {} to {} on computers' internet cards.", address, forward);
            } catch (final IOException e) {
                LOGGER.error("Failed to listen on {} to forward connections to {}.", address, forward, e);
            }
        }
    }

    private void connectForwards() {
        final long now = System.nanoTime();
        pendingForwards.removeIf(pending -> {
            final Forward forward = pending.forward();
            for (final InternetConnection connection : connections) {
                if (connection.openInbound(pending.channel(), forward.guestAddress(), forward.guestPort())) {
                    return true;
                }
            }
            if (now - pending.deadline() < 0) {
                return false;
            }
            LOGGER.info("No computer with an internet card at {} took the connection.", forward);
            closeQuietly(pending.channel());
            return true;
        });
    }

    private static void closeQuietly(final SocketChannel channel) {
        try {
            channel.close();
        } catch (final IOException e) {
            // Nothing useful to do about a channel that will not close.
        }
    }

    private void refreshFilter() {
        try {
            addressFilter.refresh();
        } catch (final Exception e) {
            LOGGER.error("Failed to refresh internet address filter.", e);
        }
    }

    private void shutdown() {
        if (!stopped.compareAndSet(false, true)) {
            return;
        }

        if (maintenanceExecutor != null) {
            maintenanceExecutor.shutdownNow();
        }

        final List<InternetConnection> open = List.copyOf(connections);
        connections.clear();

        internetThread.execute(() -> {
            runCommands();
            pendingForwards.forEach(pending -> closeQuietly(pending.channel()));
            pendingForwards.clear();
            for (final InternetConnection connection : open) {
                connection.shutdown();
            }
            socketManager.close();
        });
        internetThread.shutdown();

        try {
            if (!internetThread.awaitTermination(5, TimeUnit.SECONDS)) {
                LOGGER.warn("Internet thread did not stop in time; forcing it down.");
                internetThread.shutdownNow();
            }
        } catch (final InterruptedException e) {
            internetThread.shutdownNow();
            Thread.currentThread().interrupt();
        }

        echoExecutor.shutdownNow();
        LOGGER.info("Internet access stopped.");
    }
}
