/* blink.c - a bare-metal program that blinks a redstone output.
 *
 * For a microcontroller or drone with a redstone card. In a RISC-V computer:
 *   ocbuild -o blink.bin /mnt/builtin/example/blink.c
 *   ocflash blink.bin Blink
 * then move the EEPROM over. Powering any other side stops it.
 */

#include <oc.h>

int main(void) {
    char redstone[OC_ADDRESS_SIZE];
    int on = 0;

    if (oc_init() < 0 || oc_find("redstone", redstone) < 0) {
        oc_panic("blink needs a redstone card");
    }
    for (;;) {
        on = !on;
        oc_callf(redstone, "setOutput", "ii", 1, on ? 15 : 0); /* side 1 is up */
        /* Sleeps half a second, or until a signal arrives. */
        if (oc_pull(500) > 0 && oc_equals(0, "redstone_changed")) {
            break;
        }
    }
    oc_callf(redstone, "setOutput", "ii", 1, 0);
    return 0;
}
