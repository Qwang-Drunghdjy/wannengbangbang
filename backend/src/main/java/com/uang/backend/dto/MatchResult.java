package com.uang.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 匹配结果，包含匹配到的物品信息和相似度得分。
 * 正反向匹配共用：寻物→拾物时为 {@code MatchResult<LostItem>}，拾物→寻物时为 {@code MatchResult<FindItem>}。
 *
 * @param <T> 匹配到的物品类型（LostItem 或 FindItem）
 */
@Data
@AllArgsConstructor
public class MatchResult<T> {

    /** 匹配到的物品信息（拾物 LostItem 或 寻物 FindItem） */
    private T item;

    /** 相似度得分 (0.0 ~ 1.0) */
    private double score;
}
