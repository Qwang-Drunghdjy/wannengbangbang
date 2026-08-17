package com.uang.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI 自动生成描述请求体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DescribeImageRequest {

    /**
     * 图片 Base64 内容（不含 data URL 前缀，由前端压缩后传入）
     */
    @NotBlank(message = "图片不能为空")
    @Size(max = 4 * 1024 * 1024, message = "图片过大（超过 4MB）")
    private String imageBase64;

    /**
     * 发布类型：seek=寻物启事 / claim=拾物招领；可选，非法值按 claim 风格处理
     */
    private String category;
}
