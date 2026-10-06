package ocsquared.riscv;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

@Timeout(value = 5, unit = TimeUnit.MINUTES)
public final class FloppyTest {
    private static final long FLOPPY_SIZE = 512 * 1024;

    @TempDir
    Path directory;

    @Test
    public void floppyKeepsFilesAcrossEjecting() throws Exception {
        final Path image = directory.resolve("floppy.img");

        try (final TestMachine test = TestMachine.bootWithFloppyDrives(1)) {
            test.login();
            // The drive comes after the root disk and is empty.
            test.type("echo size-$(cat /sys/block/vdb/size)");
            test.awaitScreen("size-0");

            test.machine.setFloppy(0, FileBlockDevice.open(image, FLOPPY_SIZE));
            test.type("until [ $(cat /sys/block/vdb/size) != 0 ]; do sleep 1; done; echo in-$((1+1))");
            test.awaitScreen("in-2");
            test.type("mke2fs /dev/vdb > /dev/null && mount /dev/vdb /mnt && echo note > /mnt/note && umount /mnt && echo saved-$((2+1))");
            test.awaitScreen("saved-3");

            test.machine.setFloppy(0, null);
            test.type("until [ $(cat /sys/block/vdb/size) = 0 ]; do sleep 1; done; echo out-$((3+1))");
            test.awaitScreen("out-4");

            test.machine.setFloppy(0, FileBlockDevice.open(image, FLOPPY_SIZE));
            test.type("until [ $(cat /sys/block/vdb/size) != 0 ]; do sleep 1; done; mount /dev/vdb /mnt && echo read-$(cat /mnt/note)");
            test.awaitScreen("read-note");
        }
    }
}
