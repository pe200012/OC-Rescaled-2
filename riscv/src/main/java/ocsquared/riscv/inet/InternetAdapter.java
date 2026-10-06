/* SPDX-License-Identifier: MIT */
// Adapted from OpenComputers II (https://github.com/fnuecke/oc2), Copyright (c) Florian "Sangar" Nücke.

package ocsquared.riscv.inet;

import javax.annotation.Nullable;

public interface InternetAdapter {
    @Nullable
    byte[] readInternetFrame();

    void writeInternetFrame(byte[] frame);
}
