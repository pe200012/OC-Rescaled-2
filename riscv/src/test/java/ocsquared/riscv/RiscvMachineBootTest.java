package ocsquared.riscv;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;

@Timeout(value = 5, unit = TimeUnit.MINUTES)
public final class RiscvMachineBootTest {
    @Test
    public void bootsToShellOnTerminalAndPowersOff() throws Exception {
        final TestMachine test = TestMachine.boot();
        test.login();
        test.type("echo hi-$((6*7))");
        test.awaitScreen("hi-42");
        // The root file system is journaled, so machines stopped without shutting down keep it intact.
        test.type("echo journal-$(dmesg | grep -c 'with ordered data mode')");
        test.awaitScreen("journal-1");
        test.type("poweroff");
        test.awaitPowerOff();
        assertFalse(test.machine.isRunning());
    }
}
