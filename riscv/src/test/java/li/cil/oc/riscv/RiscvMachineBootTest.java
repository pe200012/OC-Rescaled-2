package li.cil.oc.riscv;

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
        test.type("poweroff");
        test.awaitPowerOff();
        assertFalse(test.machine.isRunning());
    }
}
