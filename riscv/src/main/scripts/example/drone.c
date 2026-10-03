/* drone.c - a bare-metal program that flies a drone around a square.
 *
 * Build and flash it like blink.c, then craft the EEPROM into a drone:
 *   ocbuild -o drone.bin /mnt/builtin/example/drone.c
 *   ocflash drone.bin Drone
 * It flies four blocks along each side, changing colour at every corner, and
 * lands back where it started.
 */

#include <oc.h>

static char drone[OC_ADDRESS_SIZE];

/* Moves relative to where it is, then waits until it gets there. */
static void fly(int dx, int dy, int dz) {
    oc_callf(drone, "move", "iii", dx, dy, dz);
    do {
        oc_sleep(100);
    } while (oc_callf(drone, "getOffset", "") > 0 && oc_double(0) > 0.1);
}

int main(void) {
    static const int colours[] = {0xFF4040, 0x40FF40, 0x4040FF, 0xFFFF40};
    static const int steps[][2] = {{4, 0}, {0, 4}, {-4, 0}, {0, -4}};

    if (oc_init() < 0 || oc_find("drone", drone) < 0) {
        oc_panic("drone.c only runs in a drone");
    }
    oc_callf(drone, "setStatusText", "s", "RISC-V!");
    fly(0, 2, 0);
    for (int i = 0; i < 4; i++) {
        oc_callf(drone, "setLightColor", "i", colours[i]);
        fly(steps[i][0], 0, steps[i][1]);
    }
    fly(0, -2, 0);
    oc_callf(drone, "setStatusText", "s", "done");
    return 0;
}
