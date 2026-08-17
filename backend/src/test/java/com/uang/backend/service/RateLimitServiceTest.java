package com.uang.backend.service;

import com.uang.backend.exception.RateLimitException;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RateLimitServiceTest {

    private RateLimitService serviceWithClock(int limit, AtomicLong clock) {
        return new RateLimitService(limit, clock::get);
    }

    @Test
    void shouldAllowRequestsWithinLimit() {
        AtomicLong clock = new AtomicLong(0);
        RateLimitService service = serviceWithClock(5, clock);
        for (int i = 0; i < 5; i++) {
            clock.addAndGet(1000);
            assertDoesNotThrow(() -> service.check(1L));
        }
    }

    @Test
    void shouldRejectWhenExceeded() {
        AtomicLong clock = new AtomicLong(0);
        RateLimitService service = serviceWithClock(5, clock);
        for (int i = 0; i < 5; i++) {
            clock.addAndGet(1000);
            service.check(1L);
        }
        assertThrows(RateLimitException.class, () -> service.check(1L));
    }

    @Test
    void shouldAllowAfterWindowExpired() {
        AtomicLong clock = new AtomicLong(0);
        RateLimitService service = serviceWithClock(1, clock);
        service.check(1L);
        assertThrows(RateLimitException.class, () -> service.check(1L));

        // 滑动窗口（60s）过后应放行
        clock.addAndGet(61_000);
        assertDoesNotThrow(() -> service.check(1L));
    }

    @Test
    void shouldTrackUsersIndependently() {
        AtomicLong clock = new AtomicLong(0);
        RateLimitService service = serviceWithClock(1, clock);
        service.check(1L);
        assertThrows(RateLimitException.class, () -> service.check(1L));

        // 用户 1 超限不影响用户 2
        assertDoesNotThrow(() -> service.check(2L));
    }
}
