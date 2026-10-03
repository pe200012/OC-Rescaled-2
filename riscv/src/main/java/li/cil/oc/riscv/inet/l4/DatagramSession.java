/* SPDX-License-Identifier: MIT */
// Adapted from OpenComputers II (https://github.com/fnuecke/oc2), Copyright (c) Florian "Sangar" Nücke.

package li.cil.oc.riscv.inet.l4;

public final class DatagramSession extends AbstractSession {
    private final SessionKey.Datagram key;
    private SessionState state = SessionState.NEW;

    // --------------------------------------------------------------------- //

    public DatagramSession(final SessionKey.Datagram key) {
        super(key.destinationIpAddress(), key.destinationPort());
        this.key = key;
    }

    // --------------------------------------------------------------------- //

    @Override
    public SessionKey.Datagram getKey() {
        return key;
    }

    @Override
    public SessionState getState() {
        return state;
    }

    public void setState(final SessionState state) {
        this.state = state;
    }

    @Override
    public void close() {
        if (state == SessionState.NEW || state == SessionState.ESTABLISHED) {
            state = SessionState.FINISH;
        }
    }

    @Override
    public void expire() {
        if (state == SessionState.NEW || state == SessionState.ESTABLISHED) {
            state = SessionState.EXPIRED;
        }
    }

    @Override
    public String toString() {
        return "DatagramSession(" + key + ")";
    }
}
