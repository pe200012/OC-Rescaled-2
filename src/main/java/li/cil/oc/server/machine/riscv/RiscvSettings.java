package li.cil.oc.server.machine.riscv;

import com.typesafe.config.Config;
import li.cil.oc.api.API;

import java.util.List;

/**
 * Settings of RISC-V machines from OpenComputers' configuration, read here as this code is
 * compiled before the Scala settings it lives in.
 */
final class RiscvSettings {
    private static final long KIBIBYTE = 1024;
    private static final long[] DEFAULT_DISK_SIZES = {16384, 32768, 65536};

    private RiscvSettings() {
    }

    /**
     * The size of a hard drive of a tier, in bytes.
     */
    static long diskSize(final int tier) {
        final Config config = API.config;
        List<Integer> sizes = List.of();
        if (config != null && config.hasPath("filesystem.hddSizes")) {
            sizes = config.getIntList("filesystem.hddSizes");
        }
        final int index = Math.max(0, tier);
        if (index < sizes.size()) {
            return sizes.get(index) * KIBIBYTE;
        }
        return DEFAULT_DISK_SIZES[Math.min(index, DEFAULT_DISK_SIZES.length - 1)] * KIBIBYTE;
    }

    /**
     * The power a machine draws per tick for each MHz it runs at.
     */
    static double costPerMegahertz() {
        return getDouble("power.cost.riscvPerMegahertz", 0.01);
    }

    /**
     * The power a machine draws per tick for each MiB of RAM it has.
     */
    static double costPerMegabyte() {
        return getDouble("power.cost.riscvPerMegabyte", 0.05);
    }

    private static double getDouble(final String path, final double fallback) {
        final Config config = API.config;
        return config != null && config.hasPath(path) ? Math.max(0, config.getDouble(path)) : fallback;
    }
}
