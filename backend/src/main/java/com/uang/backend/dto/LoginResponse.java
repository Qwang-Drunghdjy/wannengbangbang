package com.uang.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录响应体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    /** JWT 令牌 */
    private String token;

    /** 用户 ID */
    private Long userId;

    /** 昵称 */
    private String nickname;
}
