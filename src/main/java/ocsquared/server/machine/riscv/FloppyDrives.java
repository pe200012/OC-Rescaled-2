package ocsquared.server.machine.riscv;

import li.cil.oc.api.Items;
import li.cil.oc.api.detail.ItemInfo;
import li.cil.oc.api.machine.Machine;
import li.cil.oc.api.network.Network;
import li.cil.oc.api.network.Node;
import ocsquared.riscv.RiscvMachine;
import li.cil.sedna.api.device.BlockDevice;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * The floppy drives of a machine: its own floppy slot, as in computer cases and robots, then the
 * disk drives it can see, by address. Each is a drive of the RISC-V machine that holds whatever
 * floppy is in it, as a disk image named by an id stored on the floppy, like hard drives.
 * <p>
 * Which drives there are is fixed when the machine starts; floppies may come and go while it runs.
 */
final class FloppyDrives {
    private static final Logger LOGGER = LogManager.getLogger("OpenComputers/RISC-V");
    // ocsquared.Constants.ItemName.Floppy; other media, like loot disks, are not disk images.
    private static final String FLOPPY_ITEM = "floppy";
    private static final int MAX_DRIVES = 4;

    private final Machine machine;
    private final int ownSlot;
    // Disk drive component addresses, null for the machine's own slot.
    private final List<String> addresses;
    // Where each drive keeps its floppy, looked up on the server thread. Disk drives are not
    // connected yet while the machine is loaded, so this waits for the first update.
    private final IInventory[] inventories;
    private final int[] slots;
    private boolean needsLookup = true;
    // The stack each drive held at the last update, and the disk that is inserted from it.
    private final ItemStack[] seen;
    private final String[] inserted;

    private FloppyDrives(final Machine machine, final int ownSlot, final List<String> addresses) {
        this.machine = machine;
        this.ownSlot = ownSlot;
        this.addresses = addresses;
        inventories = new IInventory[addresses.size()];
        slots = new int[addresses.size()];
        seen = new ItemStack[addresses.size()];
        inserted = new String[addresses.size()];
    }

    /**
     * Finds the drives of a machine. Runs on the server thread.
     */
    static FloppyDrives find(final Machine machine) {
        final int ownSlot = machine.host() instanceof IInventory ? RiscvHooks.floppySlot.applyAsInt(machine.host()) : -1;
        final List<String> addresses = new ArrayList<>();
        if (ownSlot >= 0) {
            addresses.add(null);
        }
        addresses.addAll(NetworkBridge.findComponents(machine, "disk_drive"));
        if (addresses.size() > MAX_DRIVES) {
            LOGGER.info("RISC-V machine sees {} floppy drives, using the first {}.", addresses.size(), MAX_DRIVES);
            return new FloppyDrives(machine, ownSlot, List.copyOf(addresses.subList(0, MAX_DRIVES)));
        }
        return new FloppyDrives(machine, ownSlot, addresses);
    }

    int count() {
        return addresses.size();
    }

    /**
     * Looks the drives up again on the next update, e.g. as the machine's components changed.
     */
    void invalidate() {
        needsLookup = true;
    }

    /**
     * Whether a floppy may have been inserted or ejected, so that {@link #update} needs to run.
     * Runs on the executor thread; only compares references.
     */
    boolean needsUpdate() {
        if (needsLookup) {
            return true;
        }
        for (int i = 0; i < inventories.length; i++) {
            final ItemStack current = inventories[i] != null ? inventories[i].getStackInSlot(slots[i]) : ItemStack.EMPTY;
            if (current != seen[i]) {
                return true;
            }
        }
        return false;
    }

    /**
     * Puts the floppies that are in the drives into the machine's drives, and takes out those that
     * are gone. Runs on the server thread while the machine is not executing.
     */
    void update(final RiscvMachine vm) {
        if (needsLookup) {
            needsLookup = false;
            lookUp();
        }
        for (int i = 0; i < inventories.length; i++) {
            final IInventory inventory = inventories[i];
            final ItemStack stack = inventory != null ? inventory.getStackInSlot(slots[i]) : ItemStack.EMPTY;
            seen[i] = stack;
            final String disk = isFloppy(stack) ? RiscvStorage.diskId(stack, () -> markChanged(inventory)).toString() : null;
            if (Objects.equals(disk, inserted[i])) {
                continue;
            }
            BlockDevice floppy = null;
            try {
                if (disk != null) {
                    floppy = RiscvStorage.openFloppy(disk);
                }
                vm.setFloppy(i, floppy);
                inserted[i] = disk;
            } catch (final Exception e) {
                LOGGER.warn("Failed changing floppy of RISC-V machine.", e);
                if (floppy != null) {
                    try {
                        floppy.close();
                    } catch (final Exception ignored) {
                    }
                }
            }
        }
    }

    // --------------------------------------------------------------------- //

    private void lookUp() {
        final Node own = machine.node();
        final Network network = own != null ? own.network() : null;
        for (int i = 0; i < addresses.size(); i++) {
            final String address = addresses.get(i);
            if (address == null) {
                inventories[i] = (IInventory) machine.host();
                slots[i] = ownSlot;
                continue;
            }
            // Disk drives, also those mounted in racks, keep their floppy in their only slot.
            final Node node = network != null ? network.node(address) : null;
            inventories[i] = node != null && node.host() instanceof IInventory inventory ? inventory : null;
            slots[i] = 0;
        }
    }

    private void markChanged(final IInventory inventory) {
        if (inventory == machine.host()) {
            machine.host().markChanged();
        } else if (inventory != null) {
            inventory.markDirty();
        }
    }

    private static boolean isFloppy(final ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        final ItemInfo info = Items.get(stack);
        return info != null && FLOPPY_ITEM.equals(info.name());
    }
}
