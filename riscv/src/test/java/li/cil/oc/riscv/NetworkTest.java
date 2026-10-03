package li.cil.oc.riscv;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.TimeUnit;

@Timeout(value = 5, unit = TimeUnit.MINUTES)
public final class NetworkTest {
    @Test
    public void twoMachinesOnOneCablePingEachOther() throws Exception {
        try (final TestMachine left = TestMachine.bootWithNetwork();
             final TestMachine right = TestMachine.bootWithNetwork()) {
            // Each machine's steps also advance the other one and carry frames across the cable.
            left.afterStep = () -> {
                stepQuietly(right);
                carry(left, right);
            };
            right.afterStep = () -> {
                stepQuietly(left);
                carry(left, right);
            };

            right.login();
            right.type("ip addr add 10.0.0.2/24 dev eth0 && ip link set eth0 up && echo up-$((1+1))");
            right.awaitScreen("up-2");

            left.login();
            left.type("ip addr add 10.0.0.1/24 dev eth0 && ip link set eth0 up && echo up-$((1+1))");
            left.awaitScreen("up-2");
            left.type("ping -c 2 10.0.0.2");
            left.awaitScreen("2 packets received");
        }
    }

    private static void carry(final TestMachine left, final TestMachine right) {
        byte[] frame;
        while ((frame = left.machine.readFrame(0)) != null) {
            right.machine.writeFrame(0, frame);
        }
        while ((frame = right.machine.readFrame(0)) != null) {
            left.machine.writeFrame(0, frame);
        }
    }

    private static void stepQuietly(final TestMachine test) {
        try {
            test.stepAlone();
        } catch (final Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
