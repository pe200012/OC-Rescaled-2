/* SPDX-License-Identifier: MIT */
// Adapted from OpenComputers II (https://github.com/fnuecke/oc2), Copyright (c) Florian "Sangar" Nücke.

package ocsquared.riscv.inet.l4;

public enum SessionState {
    NEW,
    ESTABLISHED,
    FINISH,
    REJECT,
    EXPIRED;

    public boolean isClosed() {
        return this == FINISH || this == REJECT || this == EXPIRED;
    }
}
