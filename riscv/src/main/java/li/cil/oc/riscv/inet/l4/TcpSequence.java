/* SPDX-License-Identifier: MIT */
// Adapted from OpenComputers II (https://github.com/fnuecke/oc2), Copyright (c) Florian "Sangar" Nücke.

package li.cil.oc.riscv.inet.l4;

public final class TcpSequence {
    public static boolean lt(final int a, final int b) {
        return a - b < 0;
    }

    public static boolean leq(final int a, final int b) {
        return a - b <= 0;
    }

    public static boolean gt(final int a, final int b) {
        return a - b > 0;
    }

    public static boolean geq(final int a, final int b) {
        return a - b >= 0;
    }

    public static boolean between(final int value, final int low, final int high) {
        return geq(value, low) && lt(value, high);
    }

    // --------------------------------------------------------------------- //

    private TcpSequence() {
    }
}
