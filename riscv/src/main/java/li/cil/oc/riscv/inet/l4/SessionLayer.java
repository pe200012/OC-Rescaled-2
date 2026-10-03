/* SPDX-License-Identifier: MIT */
// Adapted from OpenComputers II (https://github.com/fnuecke/oc2), Copyright (c) Florian "Sangar" Nücke.

package li.cil.oc.riscv.inet.l4;

import javax.annotation.Nullable;
import java.nio.ByteBuffer;

public interface SessionLayer {
    interface Receiver {
        @Nullable
        ByteBuffer receive(AbstractSession session);

        void cancel();
    }

    // --------------------------------------------------------------------- //

    default void onTick() {
    }

    default void onStop() {
    }

    // --------------------------------------------------------------------- //

    void receiveSession(Receiver receiver);

    void sendSession(AbstractSession session, @Nullable ByteBuffer data);
}
