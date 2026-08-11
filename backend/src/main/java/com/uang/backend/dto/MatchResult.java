package com.uang.backend.dto;

import com.uang.backend.entity.LostItem;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 匹配结果，包含失物信息和相似度得分
 */
@Data
@AllArgsConstructor
public class MatchResult {

    /** 匹配到的失物信息 */
    private LostItem lostItem;

    /** 相似度得分 (0.0 ~ 1.0) */
    private double score;
}
