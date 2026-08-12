package com.uang.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 手机号，唯一，用于登录
     */
    @Column(nullable = false, unique = true, length = 20)
    private String phone;

    /**
     * BCrypt 哈希密文，不参与 JSON 序列化
     */
    @JsonIgnore
    @Column(nullable = false, length = 200)
    private String password;

    /**
     * 昵称
     */
    @Column(nullable = false, length = 50)
    private String nickname;

    /**
     * 账号状态，默认 NORMAL
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.NORMAL;

    /**
     * 注册时间，由 Service 层在创建时赋值
     */
    @Column(name = "create_time")
    private LocalDateTime createTime;

    /**
     * 账号状态枚举
     */
    public enum Status {
        NORMAL,
        BANNED
    }
}
