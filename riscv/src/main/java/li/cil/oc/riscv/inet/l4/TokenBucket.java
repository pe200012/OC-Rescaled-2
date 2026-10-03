/* SPDX-License-Identifier: MIT */
// Adapted from OpenComputers II (https://github.com/fnuecke/oc2), Copyright (c) Florian "Sangar" Nücke.

package li.cil.oc.riscv.inet.l4;

import java.util.concurrent.TimeUnit;

public final class TokenBucket {
    private final double capacity;
    private final double refillPerNano;
    private double tokens;
    private long lastRefillTime;

    // --------------------------------------------------------------------- //

    public TokenBucket(final int capacity, final double refillPerSecond) {
        this.capacity = capacity;
        this.refillPerNano = refillPerSecond / TimeUnit.SECONDS.toNanos(1);
        this.tokens = capacity;
        this.lastRefillTime = System.nanoTime();
    }

    // --------------------------------------------------------------------- //

    public boolean tryAcquire(final long now) {
        tokens = Math.min(capacity, tokens + Math.max(0, now - lastRefillTime) * refillPerNano);
        lastRefillTime = now;
        if (tokens < 1) {
            return false;
        }
        tokens -= 1;
        return true;
    }
}
