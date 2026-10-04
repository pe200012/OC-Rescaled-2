package li.cil.oc.riscv;

import li.cil.oc.riscv.inet.InternetConfig;
import li.cil.oc.riscv.inet.InternetLink;
import li.cil.oc.riscv.inet.InternetManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@Timeout(value = 5, unit = TimeUnit.MINUTES)
public final class InternetTest {
    private static final String HOST = "example.com";

    @Test
    public void guestTalksHttpThroughGateway() throws Exception {
        // Needs real internet access; resolved here so the guest needs no name server.
        final InetAddress target = findReachable();
        assumeTrue(target != null, "No internet access to test with.");

        InternetManager.start();
        final InternetLink link = new InternetLink(0, "test");
        try (final TestMachine test = TestMachine.bootWithNetwork()) {
            // Stands in for the server thread ticking the internet manager.
            test.afterStep = () -> {
                link.exchange(test.machine);
                InternetManager.getInstance().ifPresent(InternetManager::onServerTick);
            };

            test.login();
            test.type("ip addr add 10.0.2.15/24 dev eth0 && ip link set eth0 up && ip route add default via 10.0.2.2");
            test.type("printf 'HEAD / HTTP/1.0\\r\\nHost: " + HOST + "\\r\\n\\r\\n' | nc " + target.getHostAddress() + " 80 | head -n 1");
            test.awaitScreen("HTTP/1.");
        } finally {
            link.disconnect();
            InternetManager.stop();
        }
    }

    @Test
    public void guestPingsThroughGateway() throws Exception {
        assumeTrue(findReachable() != null, "No internet access to test with.");

        InternetManager.start();
        final InternetLink link = new InternetLink(0, "test");
        try (final TestMachine test = TestMachine.bootWithNetwork()) {
            test.afterStep = () -> {
                link.exchange(test.machine);
                InternetManager.getInstance().ifPresent(InternetManager::onServerTick);
            };

            test.login();
            test.type("ip addr add 10.0.2.15/24 dev eth0 && ip link set eth0 up && ip route add default via 10.0.2.2");
            test.type("ping -c 3 1.1.1.1");
            test.awaitScreen("packet loss");
            final String screen = test.screenText();
            assertTrue(!screen.contains(" 0 packets received"), screen);
        } finally {
            link.disconnect();
            InternetManager.stop();
        }
    }

    @Test
    public void forwardedConnectionReachesGuest() throws Exception {
        final int port;
        try (final ServerSocket probe = new ServerSocket(0, 1, InetAddress.getLoopbackAddress())) {
            port = probe.getLocalPort();
        }
        InternetConfig.internetForwards = List.of(port + ":10.0.2.15:7777");
        InternetManager.start();
        final InternetLink link = new InternetLink(0, "test");
        try (final TestMachine test = TestMachine.bootWithNetwork()) {
            test.afterStep = () -> {
                link.exchange(test.machine);
                InternetManager.getInstance().ifPresent(InternetManager::onServerTick);
            };

            test.login();
            test.type("ip addr add 10.0.2.15/24 dev eth0 && ip link set eth0 up");
            test.type("echo hi-from-guest | nc -l -p 7777 & sleep 1; echo list''ening");
            test.awaitScreen("listening");
            final CompletableFuture<String> client = CompletableFuture.supplyAsync(() -> talkTo(port));
            test.awaitCondition("the guest to answer", client::isDone);
            assertEquals("hi-from-guest", client.join());
            test.awaitScreen("hi-from-host");
        } finally {
            link.disconnect();
            InternetManager.stop();
            InternetConfig.internetForwards = new ArrayList<>();
        }
    }

    private static String talkTo(final int port) {
        for (int attempt = 0; attempt < 100; attempt++) {
            try (final Socket socket = new Socket(InetAddress.getLoopbackAddress(), port)) {
                socket.setSoTimeout(30_000);
                final String line = new BufferedReader(new InputStreamReader(socket.getInputStream(), UTF_8)).readLine();
                if (line != null) {
                    socket.getOutputStream().write("hi-from-host\n".getBytes(UTF_8));
                    return line;
                }
            } catch (final IOException ignored) {
                // Try again.
            }
            try {
                Thread.sleep(100);
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
        }
        return null;
    }

    private static InetAddress findReachable() {
        try {
            for (final InetAddress address : InetAddress.getAllByName(HOST)) {
                if (address instanceof Inet4Address) {
                    try (final Socket socket = new Socket()) {
                        socket.connect(new InetSocketAddress(address, 80), 3000);
                        return address;
                    }
                }
            }
        } catch (final Exception ignored) {
            // Offline.
        }
        return null;
    }
}
