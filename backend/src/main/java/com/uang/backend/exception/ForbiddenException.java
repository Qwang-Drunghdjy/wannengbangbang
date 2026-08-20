package com.uang.backend.exception;

/**
 * 越权操作异常（已登录但无权操作他人资源），由 GlobalExceptionHandler 统一转为 HTTP 403。
 */
public class ForbiddenException extends RuntimeException {

    /**
     * @param message 错误提示（如 "无权修改该物品的认领状态"）
     */
    public ForbiddenException(String message) {
        super(message);
    }
}