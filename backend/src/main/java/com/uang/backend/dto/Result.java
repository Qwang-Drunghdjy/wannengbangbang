package com.uang.backend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 统一 API 响应体
 * @param <T> 响应数据的类型
 */
@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Result<T> {

    /** 状态码 */
    private int code;

    /** 提示消息 */
    private String message;

    /** 数据体 */
    private T data;

    /**
     * 成功响应
     * @param data 数据体
     * @param <T> 数据类型
     * @return Result
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "success", data);
    }

    /**
     * 错误响应
     * @param code 业务错误码
     * @param message 错误描述
     * @param <T> 数据类型
     * @return Result
     */
    public static <T> Result<T> error(int code, String message) {
        return new Result<>(code, message, null);
    }
}
