package com.uang.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 失物信息实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "lost_item")
public class LostItem {

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
     * 拾获地点
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
    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    /**
     * 发布时间，由 Service 层在创建时赋值
     */
    @Column(name = "create_time")
    private LocalDateTime createTime;
}
