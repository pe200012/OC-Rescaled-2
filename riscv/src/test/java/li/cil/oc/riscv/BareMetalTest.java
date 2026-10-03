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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The bare-metal workflow end to end: a program is built and flashed with the tools in a Linux
 * machine, then runs on a machine of its own and talks to components through the component window.
 */
@Timeout(value = 10, unit = TimeUnit.MINUTES)
public final class BareMetalTest {
    private static final UUID EEPROM = UUID.fromString("3a1d2c4b-0000-4000-8000-00000000000e");
    private static final UUID REDSTONE = UUID.fromString("3a1d2c4b-0000-4000-8000-000000000001");

    private static final String[] PROGRAM = {
        "#include <oc.h>",
        "int main(void) {",
        "    char rs[OC_ADDRESS_SIZE];",
        "    if (oc_init() < 0 || oc_find(\"redstone\", rs) < 0) oc_panic(oc_error());",
        "    oc_callf(rs, \"setOutput\", \"ii\", 1, 15);",
        "    if (oc_pull(-1) <= 0) oc_panic(\"no signal\");",
        "    oc_callf(rs, \"setOutput\", \"isd\", 2, oc_string(0), oc_double(1) * 2);",
        "    return 0;",
        "}",
    };

    private static final String[] CRASH = {
        "int main(void) { *(volatile int *) 8 = 1; return 0; }",
    };

    /**
     * An EEPROM that keeps what is written to it, writing only on the main thread like the real
     * one, and a redstone card that logs its calls.
     */
    private static final class FakeDevices implements DeviceBus.Devices {
        final List<String> calls = new ArrayList<>();
        final List<byte[]> flashed = new ArrayList<>();
        private final boolean hasEeprom;

        FakeDevices(final boolean hasEeprom) {
            this.hasEeprom = hasEeprom;
        }

        @Override
        public int generation() {
            return 1;
        }

        @Override
        public List<DeviceBus.DeviceInfo> list() {
            final List<DeviceBus.DeviceInfo> devices = new ArrayList<>();
            if (hasEeprom) {
                devices.add(new DeviceBus.DeviceInfo(EEPROM, List.of("eeprom")));
            }
            devices.add(new DeviceBus.DeviceInfo(REDSTONE, List.of("redstone")));
            return devices;
        }

        @Override
        public List<DeviceBus.MethodInfo> methods(final UUID device) {
            return List.of();
        }

        @Override
        public Object[] invoke(final UUID device, final String method, final Object[] arguments, final boolean isMainThread) throws Exception {
            if (REDSTONE.equals(device) && method.equals("setOutput")) {
                calls.add("setOutput" + Arrays.toString(arguments));
                return new Object[]{0.0};
            }
            if (EEPROM.equals(device) && hasEeprom) {
                switch (method) {
                    case "getSize" -> {
                        return new Object[]{64 * 1024};
                    }
                    case "set" -> {
                        if (!isMainThread) {
                            throw DeviceBus.MainThreadRequired.INSTANCE;
                        }
                        flashed.add((byte[]) arguments[0]);
                        return null;
                    }
                    case "setLabel" -> {
                        return new Object[]{arguments[0]};
                    }
                    default -> {
                    }
                }
            }
            throw new NoSuchMethodException();
        }
    }

    @Test
    public void programsBuiltInLinuxDriveComponents() throws Exception {
        final List<byte[]> images = buildInLinux(PROGRAM, CRASH);

        final FakeDevices devices = new FakeDevices(false);
        try (final TestMachine test = TestMachine.bareMetal(images.get(0), devices)) {
            test.awaitCondition("the first call", () -> !devices.calls.isEmpty());
            assertEquals(List.of("setOutput[1, 15]"), devices.calls);

            // The program sleeps until the signal arrives.
            test.machine.getWindow().sendSignal("hello", new Object[]{1.25});
            test.awaitPowerOff();
            assertEquals(null, test.machine.getWindow().takePanic());
            assertEquals(List.of("setOutput[1, 15]", "setOutput[2, hello, 2.5]"), devices.calls);
        }

        try (final TestMachine test = TestMachine.bareMetal(images.get(1), new FakeDevices(false))) {
            test.awaitPowerOff();
            final String panic = test.machine.getWindow().takePanic();
            assertNotNull(panic);
            assertTrue(panic.startsWith("store access fault at 0x80"), panic);
        }
    }

    /**
     * Types each program into a Linux machine, builds it with ocbuild and flashes it with ocflash.
     */
    private static List<byte[]> buildInLinux(final String[]... programs) throws Exception {
        final FakeDevices devices = new FakeDevices(true);
        try (final TestMachine test = TestMachine.boot(devices)) {
            test.login();
            for (int i = 0; i < programs.length; i++) {
                test.type("cat > p" + i + ".c << 'EOF'");
                for (final String line : programs[i]) {
                    test.type(line);
                }
                test.type("EOF");
                test.type("clear; ocbuild -o p" + i + ".bin p" + i + ".c && ocflash p" + i + ".bin && echo flashed-$((" + i + "+100))");
                test.awaitScreen("flashed-" + (i + 100));
                assertTrue(test.screenText().contains("Wrote "), test.screenText());
            }
            assertEquals(programs.length, devices.flashed.size());
            return devices.flashed;
        }
    }
}
