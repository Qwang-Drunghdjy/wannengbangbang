package com.uang.backend.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具类：生成 / 解析 / 过期判断
 */
@Component
public class JwtUtil {

    private final SecretKey key;
    private final long expireMillis;

    public JwtUtil(@Value("${jwt.secret}") String secret,
                   @Value("${jwt.expire-days:7}") long expireDays) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expireMillis = expireDays * 24 * 60 * 60 * 1000;
    }

    /**
     * 生成 JWT 令牌
     * @param userId 用户 ID（写入 sub）
     * @param phone 手机号（写入 claim）
     * @return JWT 字符串
     */
    public String generateToken(Long userId, String phone) {
        Date now = new Date();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("phone", phone)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expireMillis))
                .signWith(key)
                .compact();
    }

    /**
     * 解析并校验 JWT，返回 Claims
     * @param token JWT 字符串
     * @return Claims（含 sub / phone / expiration）
     * @throws JwtException 令牌无效或已过期
     * @throws IllegalArgumentException token 为空等非法输入
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 判断令牌是否无效或已过期
     * @param token JWT 字符串
     * @return true 表示无效或已过期
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = parseToken(token);
            Date expiration = claims.getExpiration();
            return expiration == null || expiration.before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return true;
        }
    }
}
