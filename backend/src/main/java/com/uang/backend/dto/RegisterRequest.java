package com.uang.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 注册请求体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    /** 手机号，唯一 */
    private String phone;

    /** 密码（明文，服务端 BCrypt 加密后存储） */
    private String password;

    /** 昵称 */
    private String nickname;
}
