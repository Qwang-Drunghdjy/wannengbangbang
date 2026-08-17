package com.uang.backend.exception;

/**
 * 未认证 / 登录过期异常，由 GlobalExceptionHandler 统一转为 HTTP 401。
 */
public class UnauthorizedException extends RuntimeException {

    /**
     * @param message 错误提示（如 "未登录或登录已过期"）
     */
    public UnauthorizedException(String message) {
        super(message);
    }
}
