package li.cil.oc.server.machine.riscv;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

/**
 * The clock rate a CPU runs RISC-V machines at, kept on the CPU item. Players switch between the
 * rates by using the CPU while sneaking; faster ones draw more power.
 */
public final class CpuClock {
    public static final int[] MEGAHERTZ = {25, 50, 100, 200};
    private static final int DEFAULT_MEGAHERTZ = 50;
    private static final String TAG = "oc:clock";

    private CpuClock() {
    }

    public static int megahertz(final ItemStack stack) {
        final NBTTagCompound nbt = stack.getTagCompound();
        final int value = nbt != null ? nbt.getInteger(TAG) : 0;
        for (final int megahertz : MEGAHERTZ) {
            if (megahertz == value) {
                return value;
            }
        }
        return DEFAULT_MEGAHERTZ;
    }

    /**
     * Switches the CPU to the next clock rate, after the fastest back to the slowest.
     *
     * @return the new rate, in MHz.
     */
    public static int cycle(final ItemStack stack) {
        final int current = megahertz(stack);
        int next = MEGAHERTZ[0];
        for (int i = 0; i < MEGAHERTZ.length - 1; i++) {
            if (MEGAHERTZ[i] == current) {
                next = MEGAHERTZ[i + 1];
            }
        }
        if (!stack.hasTagCompound()) {
            stack.setTagCompound(new NBTTagCompound());
        }
        stack.getTagCompound().setInteger(TAG, next);
        return next;
    }
}
