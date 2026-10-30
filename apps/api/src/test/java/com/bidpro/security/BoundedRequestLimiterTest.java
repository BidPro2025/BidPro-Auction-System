package com.bidpro.security;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class BoundedRequestLimiterTest {
    @Test void rejectsTheExhaustedWindowWithRetryAndAllowsItAfterExpiry() {
        MutableClock clock = new MutableClock();
        var limiter = new BoundedRequestLimiter(clock, 10);
        assertThat(limiter.acquire("ip", 2, 60000).allowed()).isTrue();
        assertThat(limiter.acquire("ip", 2, 60000).allowed()).isTrue();
        var blocked = limiter.acquire("ip", 2, 60000);
        assertThat(blocked.allowed()).isFalse(); assertThat(blocked.retryAfterSeconds()).isEqualTo(60);
        clock.now.set(60000);
        assertThat(limiter.acquire("ip", 2, 60000).allowed()).isTrue();
    }

    @Test void cardinalityIsBoundedWithoutEvictingAnActiveAttackerWindow() {
        MutableClock clock = new MutableClock();
        var limiter = new BoundedRequestLimiter(clock, 2);
        assertThat(limiter.acquire("one", 1, 60000).allowed()).isTrue();
        assertThat(limiter.acquire("two", 1, 60000).allowed()).isTrue();
        assertThat(limiter.acquire("three", 1, 60000).allowed()).isFalse();
        assertThat(limiter.acquire("one", 1, 60000).allowed()).isFalse();
        clock.now.set(60000);
        assertThat(limiter.acquire("three", 1, 60000).allowed()).isTrue();
    }

    @Test void concurrentRequestsCannotExceedTheLimit() throws Exception {
        var limiter = new BoundedRequestLimiter(Clock.systemUTC(), 10);
        var executor = Executors.newFixedThreadPool(12);
        try {
            var attempts = new ArrayList<Callable<Boolean>>();
            for (int i = 0; i < 100; i++) attempts.add(() -> limiter.acquire("one", 10, 60000).allowed());
            long accepted = 0;
            for (var result : executor.invokeAll(attempts)) if (result.get()) accepted++;
            assertThat(accepted).isEqualTo(10);
        } finally { executor.shutdownNow(); }
    }

    private static class MutableClock extends Clock {
        final AtomicLong now = new AtomicLong();
        @Override public ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(ZoneId zone) { return this; }
        @Override public Instant instant() { return Instant.ofEpochMilli(now.get()); }
        @Override public long millis() { return now.get(); }
    }
}
