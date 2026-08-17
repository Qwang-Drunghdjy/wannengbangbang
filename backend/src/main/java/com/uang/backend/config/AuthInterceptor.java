package com.uang.backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uang.backend.dto.Result;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

/**
 * 认证拦截器：仅拦截 POST 请求，校验 Authorization: Bearer &lt;token&gt;，
 * 成功后将 userId 注入 request attribute，失败返回 HTTP 401。
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    /** 注入 request attribute 的用户 ID 键名 */
    public static final String USER_ID_ATTR = "userId";

    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    public AuthInterceptor(JwtUtil jwtUtil, ObjectMapper objectMapper) {
        this.jwtUtil = jwtUtil;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // GET 等非写操作全部放行
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        Long userId = extractUserId(request, jwtUtil);
        if (userId == null) {
            writeUnauthorized(response);
            return false;
        }
        request.setAttribute(USER_ID_ATTR, userId);
        return true;
    }

    /**
     * 从请求头解析 Bearer token 并返回用户 ID。
     * 供 GET 等非拦截请求在需要登录态时（如列表接口的 mine=true）手动解析复用。
     * @param request HTTP 请求
     * @param jwtUtil JWT 工具
     * @return 用户 ID；无 token 或 token 无效/已过期时返回 null
     */
    public static Long extractUserId(HttpServletRequest request, JwtUtil jwtUtil) {
        String header = request.getHeader(AUTH_HEADER);
        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            return null;
        }
        String token = header.substring(BEARER_PREFIX.length());
        try {
            Claims claims = jwtUtil.parseToken(token);
            return Long.valueOf(claims.getSubject());
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * 写入 401 响应：HTTP 状态 401 + Result.error(401, ...) JSON
     */
    private void writeUnauthorized(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(Result.error(401, "未登录或登录已过期")));
    }
}
