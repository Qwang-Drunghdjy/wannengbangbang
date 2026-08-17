package com.uang.backend.controller;

import com.uang.backend.dto.Result;
import com.uang.backend.exception.RateLimitException;
import com.uang.backend.exception.UnauthorizedException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理，将异常统一转换为 Result 响应体
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public Result<Void> handleRuntimeException(RuntimeException e) {
        return Result.error(500, e.getMessage());
    }

    /**
     * 未登录 / 登录过期：HTTP 401 + Result.error(401, message)
     */
    @ExceptionHandler(UnauthorizedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<Void> handleUnauthorized(UnauthorizedException e) {
        return Result.error(401, e.getMessage());
    }

    /**
     * 参数校验失败：HTTP 400 + Result.error(400, 首个字段错误信息)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(fieldError -> fieldError.getDefaultMessage())
                .orElse("参数错误");
        return Result.error(400, message);
    }

    /**
     * 调用过于频繁：HTTP 429 + Result.error(429, message)
     */
    @ExceptionHandler(RateLimitException.class)
    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
    public Result<Void> handleRateLimit(RateLimitException e) {
        return Result.error(429, e.getMessage());
    }
}
