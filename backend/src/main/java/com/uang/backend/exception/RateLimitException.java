package com.uang.backend.exception;

/**
 * 调用过于频繁异常（→ HTTP 429）
 */
public class RateLimitException extends RuntimeException {

    public RateLimitException(String message) {
        super(message);
    }
}
