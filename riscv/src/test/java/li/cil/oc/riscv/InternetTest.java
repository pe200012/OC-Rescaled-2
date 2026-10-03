package li.cil.oc.riscv;

import li.cil.oc.riscv.inet.InternetLink;
import li.cil.oc.riscv.inet.InternetManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

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
