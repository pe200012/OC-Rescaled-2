package li.cil.oc.riscv;

import li.cil.oc.riscv.bus.DeviceBus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Timeout(value = 5, unit = TimeUnit.MINUTES)
public final class DeviceBusTest {
    private static final UUID REDSTONE = UUID.fromString("3a1d2c4b-0000-4000-8000-000000000001");

    /**
     * A redstone card: setOutput may run anywhere, getInput only on the main thread.
     */
    private static final class FakeDevices implements DeviceBus.Devices {
        final List<String> calls = new ArrayList<>();

        @Override
        public int generation() {
            return 1;
        }

        @Override
        public List<DeviceBus.DeviceInfo> list() {
            return List.of(new DeviceBus.DeviceInfo(REDSTONE, List.of("redstone")));
        }

        @Override
        public List<DeviceBus.MethodInfo> methods(final UUID device) {
            return REDSTONE.equals(device)
                ? List.of(new DeviceBus.MethodInfo("setOutput", "function(side, value)"), new DeviceBus.MethodInfo("getInput", null))
                : null;
        }

        @Override
        public Object[] invoke(final UUID device, final String method, final Object[] arguments, final boolean isMainThread) throws Exception {
            switch (method) {
                case "setOutput" -> {
                    calls.add("setOutput" + Arrays.toString(arguments));
                    return new Object[]{0.0};
                }
                case "getInput" -> {
                    if (!isMainThread) {
                        throw DeviceBus.MainThreadRequired.INSTANCE;
                    }
                    calls.add("getInput" + Arrays.toString(arguments) + " on main thread");
                    return new Object[]{7};
                }
                default -> throw new NoSuchMethodException();
            }
        }
    }

    @Test
    public void micropythonCallsComponentMethods() throws Exception {
        final FakeDevices devices = new FakeDevices();
        final TestMachine test = new TestMachine(devices);

        test.login();
        test.type("micropython -c \"from devices import bus; r=bus.find('redstone'); print('set', r.setOutput(1, 15)); print('in', r.getInput(3))\"");
        test.awaitScreen("in 7");

        final String screen = test.screenText();
        // Numbers pass through the guest's bus daemon, which writes 0.0 back as 0.
        assertTrue(screen.lines().anyMatch(line -> line.strip().equals("set 0")), screen);
        assertEquals(List.of("setOutput[1.0, 15.0]", "getInput[3.0] on main thread"), devices.calls);
    }
}
