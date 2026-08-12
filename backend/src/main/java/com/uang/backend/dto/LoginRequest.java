package com.uang.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录请求体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    /** 手机号 */
    private String phone;

    /** 密码（明文，服务端用 BCrypt 校验） */
    private String password;
}
