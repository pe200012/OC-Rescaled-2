package li.cil.oc.riscv;

import li.cil.oc.riscv.bus.DeviceBus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Timeout(value = 5, unit = TimeUnit.MINUTES)
public final class DeviceBusTest {
    private static final UUID REDSTONE = UUID.fromString("3a1d2c4b-0000-4000-8000-000000000001");
    private static final UUID ROBOT = UUID.fromString("3a1d2c4b-0000-4000-8000-000000000002");

    /**
     * A redstone card: setOutput may run anywhere, getInput only on the main thread. And a robot
     * stuck in front of a block.
     */
    private static final class FakeDevices implements DeviceBus.Devices {
        final List<String> calls = new ArrayList<>();

        @Override
        public int generation() {
            return 1;
        }

        @Override
        public List<DeviceBus.DeviceInfo> list() {
            return List.of(new DeviceBus.DeviceInfo(REDSTONE, List.of("redstone")), new DeviceBus.DeviceInfo(ROBOT, List.of("robot")));
        }

        @Override
        public List<DeviceBus.MethodInfo> methods(final UUID device) {
            if (ROBOT.equals(device)) {
                return List.of(new DeviceBus.MethodInfo("move", null), new DeviceBus.MethodInfo("turn", null), new DeviceBus.MethodInfo("swing", null));
            }
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
                    calls.add("getInput" + Arrays.toString(Arrays.stream(arguments)
                        .map(a -> a instanceof byte[] bytes ? HexFormat.of().formatHex(bytes) : a).toArray()) + " on main thread");
                    return new Object[]{7};
                }
                case "move", "turn", "swing" -> {
                    calls.add(method + Arrays.toString(arguments));
                    return method.equals("move") ? new Object[]{null, "impossible move"} : new Object[]{true};
                }
                default -> throw new NoSuchMethodException();
            }
        }
    }

    @Test
    public void micropythonCallsComponentMethods() throws Exception {
        final FakeDevices devices = new FakeDevices();
        final TestMachine test = TestMachine.boot(devices);

        test.login();
        test.type("micropython -c \"from devices import bus; r=bus.find('redstone'); print('set', r.setOutput(1, 15)); "
            + "print('in', r.getInput(3)); print('bin', r.getInput(bus.blob(b'\\xff\\x00\\x01')))\"");
        test.awaitScreen("bin 7");

        final String screen = test.screenText();
        // Numbers pass through the guest's bus daemon, which writes 0.0 back as 0.
        assertTrue(screen.lines().anyMatch(line -> line.strip().equals("set 0")), screen);
        // Binary goes along to calls that wait for the main thread.
        assertEquals(List.of("setOutput[1.0, 15.0]", "getInput[3.0] on main thread", "getInput[ff0001] on main thread"), devices.calls);
    }

    @Test
    public void robotLibraryCallsRobot() throws Exception {
        final FakeDevices devices = new FakeDevices();
        final TestMachine test = TestMachine.boot(devices);

        test.login();
        test.type("micropython -c \"import robot; print('moved', robot.forward(), 'turned', robot.turn_left(), 'swung', robot.swing(robot.UP))\"");
        test.awaitScreen("moved None turned True swung True");
        assertEquals(List.of("move[3.0]", "turn[false]", "swing[1.0, null, false]"), devices.calls);
    }
}
