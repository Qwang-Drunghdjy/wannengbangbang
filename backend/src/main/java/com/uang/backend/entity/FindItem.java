package com.uang.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 寻物信息实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "find_item")
public class FindItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 物品名称
     */
    @Column(nullable = false, length = 100)
    private String title;

    /**
     * 物品描述
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * 丢失地点
     */
    @Column(length = 200)
    private String location;

    /**
     * 联系方式
     */
    @Column(length = 100)
    private String contact;

    /**
     * 图片存储的 URL
     */
    @Column(name = "image_url", length = 500)
    private String imageUrl;

    /**
     * 发布时间，由 Service 层在创建时赋值
     */
    @Column(name = "create_time")
    private LocalDateTime createTime;

    /**
     * 发布者，关联用户表
     */
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
