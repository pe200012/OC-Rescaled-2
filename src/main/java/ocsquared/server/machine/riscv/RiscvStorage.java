package ocsquared.server.machine.riscv;

import li.cil.oc.api.Driver;
import li.cil.oc.api.driver.DriverItem;
import li.cil.oc.api.driver.item.Slot;
import li.cil.oc.api.machine.MachineHost;
import ocsquared.riscv.FileBlockDevice;
import ocsquared.riscv.RiscvMachine;
import li.cil.sedna.api.device.BlockDevice;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.DimensionManager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Where RISC-V machines keep their data in the world save: disk images, named by an id stored on
 * the hard drive or floppy item, and machine snapshots, named by the machine's address.
 */
final class RiscvStorage {
    private static final String DIRECTORY = "opencomputers-riscv";
    private static final String DISK_TAG = "oc:riscvDisk";

    private RiscvStorage() {
    }

    static Path snapshot(final String machineAddress) {
        return root().resolve("machines").resolve(machineAddress + ".bin");
    }

    /**
     * Opens a disk for each hard drive installed in the host, in slot order. A new first disk gets
     * the bundled root file system installed. Runs on the server thread, as it may tag items.
     */
    static List<BlockDevice> openDisks(final MachineHost host) throws IOException {
        final List<BlockDevice> disks = new ArrayList<>();
        try {
            for (final ItemStack stack : host.internalComponents()) {
                final DriverItem driver = stack.isEmpty() ? null : Driver.driverFor(stack);
                if (driver == null || !Slot.HDD.equals(driver.slot(stack))) {
                    continue;
                }

                final Path image = disk(diskId(stack, host::markChanged).toString());
                final boolean isNew = !Files.exists(image);
                final FileBlockDevice disk = FileBlockDevice.open(image, RiscvSettings.diskSize(driver.tier(stack)));
                disks.add(disk);
                if (isNew && disks.size() == 1) {
                    try (final InputStream rootfs = RiscvMachine.openRootFilesystemImage()) {
                        disk.writeImage(rootfs);
                    }
                }
            }
        } catch (final IOException | RuntimeException e) {
            for (final BlockDevice disk : disks) {
                disk.close();
            }
            throw e;
        }
        return disks;
    }

    /**
     * Opens the disk image of a floppy, given its id from {@link #diskId}.
     */
    static FileBlockDevice openFloppy(final String id) throws IOException {
        return FileBlockDevice.open(disk(id), RiscvSettings.floppySize());
    }

    /**
     * The id of the disk image of a hard drive or floppy. Items get one the first time they are
     * used, which is saved by marking their inventory changed.
     */
    static UUID diskId(final ItemStack stack, final Runnable markChanged) {
        if (!stack.hasTagCompound()) {
            stack.setTagCompound(new NBTTagCompound());
        }
        final NBTTagCompound tag = stack.getTagCompound();
        if (tag.hasKey(DISK_TAG)) {
            try {
                return UUID.fromString(tag.getString(DISK_TAG));
            } catch (final IllegalArgumentException ignored) {
                // Damaged tag, give the drive a new disk below.
            }
        }
        final UUID id = UUID.randomUUID();
        tag.setString(DISK_TAG, id.toString());
        markChanged.run();
        return id;
    }

    // --------------------------------------------------------------------- //

    private static Path disk(final String id) {
        return root().resolve("disks").resolve(id + ".img");
    }

    private static Path root() {
        final File save = DimensionManager.getCurrentSaveRootDirectory();
        if (save == null) {
            throw new IllegalStateException("No world is loaded.");
        }
        return save.toPath().resolve(DIRECTORY);
    }
}
