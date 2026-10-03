/* SPDX-License-Identifier: MIT */
// Adapted from OpenComputers II (https://github.com/fnuecke/oc2), Copyright (c) Florian "Sangar" Nücke.

package li.cil.oc.riscv.inet.l2;

/**
 * A MAC address, split so that it fits in two primitives.
 *
 * @param prefix  the leading two octets
 * @param address the trailing four octets
 */
public record MacAddress(short prefix, int address) {
}
