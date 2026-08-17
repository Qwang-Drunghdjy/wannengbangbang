package com.uang.backend.service;

import com.uang.backend.exception.RateLimitException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * 内存滑动窗口限流：按用户维度限制每分钟调用次数。
 * 时钟通过 {@link Supplier} 注入，便于测试。
 */
@Service
public class RateLimitService {

    /** 滑动窗口时长（毫秒） */
    private static final long WINDOW_MILLIS = 60_000;

    private final int limitPerMinute;
    private final Map<Long, Deque<Long>> records = new ConcurrentHashMap<>();
    private final Supplier<Long> clock;

    @Autowired
    public RateLimitService(@Value("${glm.rate-limit-per-minute:5}") int limitPerMinute) {
        this(limitPerMinute, System::currentTimeMillis);
    }

    /**
     * 测试专用构造：可注入可控时钟
     */
    RateLimitService(int limitPerMinute, Supplier<Long> clock) {
        this.limitPerMinute = limitPerMinute;
        this.clock = clock;
    }

    /**
     * 检查并记录一次调用；窗口内超过上限抛 {@link RateLimitException}
     * @param userId 用户 ID（限流维度）
     */
    public void check(Long userId) {
        long now = clock.get();
        Deque<Long> queue = records.computeIfAbsent(userId, k -> new ArrayDeque<>());
        synchronized (queue) {
            // 清理窗口外的时间戳
            while (!queue.isEmpty() && now - queue.peekFirst() > WINDOW_MILLIS) {
                queue.pollFirst();
            }
            if (queue.size() >= limitPerMinute) {
                throw new RateLimitException("操作过于频繁，请稍后再试");
            }
            queue.addLast(now);
        }
    }
}
