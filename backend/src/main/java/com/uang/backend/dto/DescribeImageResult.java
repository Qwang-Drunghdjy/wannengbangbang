package com.uang.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI 自动生成描述结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DescribeImageResult {

    /** 物品名称建议（前端可一键填入 title） */
    private String title;

    /** 关键词描述（前端可一键填入 description） */
    private String description;
}
