/* ocflash - writes an image to the EEPROM, or reads it back. See bin/ocflash. */

#define OC_REPLY_SIZE (80 * 1024)
#include <oc.h>

static unsigned char image[OC_REPLY_SIZE];

static int fail(const char *what) {
    fprintf(stderr, "ocflash: %s: %s\n", what, oc_error());
    return 1;
}

int main(int argc, char **argv) {
    char eeprom[OC_ADDRESS_SIZE];
    const int reading = argc == 3 && !strcmp(argv[1], "-r");
    FILE *file;
    long size, capacity;

    if (argc < 2 || argc > 3) {
        fprintf(stderr, "Usage: ocflash program.bin [label]\n       ocflash -r backup.bin\n");
        return 1;
    }
    if (oc_init() < 0) return fail("component window");
    if (oc_find("eeprom", eeprom) < 0) return fail("eeprom");

    if (reading) {
        if (oc_callf(eeprom, "get", "") < 0) return fail("reading");
        file = fopen(argv[2], "wb");
        if (!file || fwrite(oc_string(0), 1, oc_length(0), file) != oc_length(0)) {
            fprintf(stderr, "ocflash: cannot write %s\n", argv[2]);
            return 1;
        }
        fclose(file);
        printf("Read %ld bytes from %s.\n", (long) oc_length(0), eeprom);
        return 0;
    }

    file = fopen(argv[1], "rb");
    if (!file) {
        fprintf(stderr, "ocflash: cannot open %s\n", argv[1]);
        return 1;
    }
    size = (long) fread(image, 1, sizeof image, file);
    fclose(file);
    if (oc_callf(eeprom, "getSize", "") < 0) return fail("eeprom");
    capacity = (long) oc_int(0);
    if (size <= 0 || size > capacity) {
        fprintf(stderr, "ocflash: %s has %ld bytes, the EEPROM holds %ld\n", argv[1], size, capacity);
        return 1;
    }
    if (oc_callf(eeprom, "set", "y", image, (size_t) size) < 0) return fail("writing");
    if (oc_type(1) == OC_STRING) {
        fprintf(stderr, "ocflash: writing: %s\n", oc_string(1));
        return 1;
    }
    if (argc == 3 && oc_callf(eeprom, "setLabel", "s", argv[2]) < 0) return fail("labelling");
    printf("Wrote %ld bytes to %s.\n", size, eeprom);
    return 0;
}
