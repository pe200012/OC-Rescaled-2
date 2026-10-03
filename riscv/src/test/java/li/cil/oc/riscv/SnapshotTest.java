package li.cil.oc.riscv;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.io.TempDir;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Timeout(value = 5, unit = TimeUnit.MINUTES)
public final class SnapshotTest {
    private static final long DISK_SIZE = 16 * 1024 * 1024;
    private static final long SNAPSHOT_ID = 0x1234_5678_9ABCL;

    @TempDir
    Path directory;

    @Test
    public void restoredMachineKeepsRunningWhereItStopped() throws Exception {
        final Path diskImage = directory.resolve("disk.img");
        final Path snapshot = directory.resolve("machine.bin");

        try (final TestMachine test = TestMachine.boot(List.of(newRootDisk(diskImage)))) {
            test.login();
            // Lives only in the shell's memory, so it survives only if the process does.
            test.type("MARK=still-$((40+2)); echo on-disk > /root/file; sync");
            // A white pixel in the top left corner of the framebuffer.
            test.type("printf '\\377\\377\\377\\0' > /dev/fb0");
            test.type("echo snap-$((1+1))");
            test.awaitScreen("snap-2");
            test.snapshot(snapshot, SNAPSHOT_ID);
        }

        try (final TestMachine test = TestMachine.restore(snapshot, SNAPSHOT_ID, List.of(FileBlockDevice.open(diskImage, DISK_SIZE)))) {
            test.type("echo $MARK $(cat /root/file)");
            test.awaitScreen("still-42 on-disk");
            test.machine.getFramebuffer().markChanged();
            final Framebuffer.Rows rows = test.machine.getFramebuffer().takeChanges();
            assertNotNull(rows);
            assertEquals(0, rows.first());
            assertEquals(0xFFFFFF, rows.colors()[0]);
        }
    }

    @Test
    public void snapshotWithOtherIdIsIgnored() throws Exception {
        final Path diskImage = directory.resolve("disk.img");
        final Path snapshot = directory.resolve("machine.bin");

        try (final TestMachine test = TestMachine.boot(List.of(newRootDisk(diskImage)))) {
            test.awaitScreen("login:");
            test.snapshot(snapshot, SNAPSHOT_ID);
        }

        final RiscvMachine machine = new RiscvMachine(32 * 1024 * 1024, RiscvMachine.linuxBootloader(), List.of(FileBlockDevice.open(diskImage, DISK_SIZE)), 0, 0);
        try (machine) {
            assertFalse(MachineSnapshot.read(snapshot, SNAPSHOT_ID + 1, machine, null, null));
        }
    }

    private static FileBlockDevice newRootDisk(final Path image) throws Exception {
        final FileBlockDevice disk = FileBlockDevice.open(image, DISK_SIZE);
        try (final InputStream rootfs = RiscvMachine.openRootFilesystemImage()) {
            disk.writeImage(rootfs);
        }
        return disk;
    }
}
