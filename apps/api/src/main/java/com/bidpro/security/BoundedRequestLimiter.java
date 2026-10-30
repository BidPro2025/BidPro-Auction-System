package com.bidpro.security;

import java.time.Clock;
import java.util.HashMap;
import java.util.Map;

/** A bounded single-process abuse gate. Multi-node deployments also need a shared edge limit. */
public final class BoundedRequestLimiter {
    private final Clock clock;
    private final int capacity;
    private final Map<String, Window> windows = new HashMap<>();

    public BoundedRequestLimiter(Clock clock, int capacity) {
        if (capacity < 1) throw new IllegalArgumentException("Limiter capacity must be positive");
        this.clock = clock;
        this.capacity = capacity;
    }

    public synchronized Decision acquire(String key, int limit, long intervalMillis) {
        if (limit < 1 || intervalMillis < 1) throw new IllegalArgumentException("Invalid rate limit");
        long now = clock.millis();
        Window current = windows.get(key);
        if (current == null || current.expiresAt <= now) {
            if (windows.size() >= capacity) windows.entrySet().removeIf(entry -> entry.getValue().expiresAt <= now);
            if (current == null && windows.size() >= capacity) return new Decision(false, 1);
            current = new Window(now + intervalMillis);
            windows.put(key, current);
        }
        if (current.count >= limit) {
            return new Decision(false, Math.max(1, (current.expiresAt - now + 999) / 1000));
        }
        current.count++;
        return new Decision(true, 0);
    }

    public record Decision(boolean allowed, long retryAfterSeconds) {}
    private static final class Window {
        private final long expiresAt;
        private int count;
        private Window(long expiresAt) { this.expiresAt = expiresAt; }
    }
}
