package com.uang.backend.service;

import com.hankcs.hanlp.HanLP;
import com.uang.backend.dto.MatchResult;
import com.uang.backend.entity.FindItem;
import com.uang.backend.entity.LostItem;
import com.uang.backend.repository.LostItemRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 智能匹配服务：基于 HanLP 分词 + Jaccard 相似度，
 * 为寻物启事推荐最相似的失物信息。
 */
@Service
public class MatchingService {

    private static final double TITLE_WEIGHT = 0.6;
    private static final double DESCRIPTION_WEIGHT = 0.3;
    private static final double LOCATION_WEIGHT = 0.1;

    private final FindItemService findItemService;
    private final LostItemRepository lostItemRepository;

    public MatchingService(FindItemService findItemService,
                           LostItemRepository lostItemRepository) {
        this.findItemService = findItemService;
        this.lostItemRepository = lostItemRepository;
    }

    /**
     * 对文本进行 HanLP 分词，返回词集合。
     * @param text 待分词文本
     * @return 词的不可变集合
     */
    public Set<String> tokenize(String text) {
        if (text == null || text.isBlank()) {
            return Collections.emptySet();
        }
        return HanLP.segment(text).stream()
                .map(term -> term.word)
                .collect(Collectors.toUnmodifiableSet());
    }

    /**
     * 计算两个词集合的 Jaccard 相似度。
     * Jaccard(A, B) = |A ∩ B| / |A ∪ B|
     * @param a 词集合 A
     * @param b 词集合 B
     * @return 相似度 [0.0, 1.0]
     */
    public double jaccard(Set<String> a, Set<String> b) {
        if (a.isEmpty() && b.isEmpty()) {
            return 0.0;
        }
        Set<String> intersection = new HashSet<>(a);
        intersection.retainAll(b);

        Set<String> union = new HashSet<>(a);
        union.addAll(b);

        return (double) intersection.size() / union.size();
    }

    /**
     * 计算单个字段的相似度。
     * 若任一方为 null 或空字符串，返回 0。
     * @param a FindItem 的字段值
     * @param b LostItem 的字段值
     * @return 该字段的 Jaccard 相似度
     */
    public double fieldSimilarity(String a, String b) {
        if (a == null || a.isBlank() || b == null || b.isBlank()) {
            return 0.0;
        }
        return jaccard(tokenize(a), tokenize(b));
    }

    /**
     * 计算 FindItem 与 LostItem 的加权总分。
     * score = 0.6 × Jaccard(title) + 0.3 × Jaccard(desc) + 0.1 × Jaccard(loc)
     * @param fi 寻物启事
     * @param li 失物信息
     * @return 加权相似度得分 [0.0, 1.0]
     */
    public double calculateScore(FindItem fi, LostItem li) {
        double titleSim = fieldSimilarity(fi.getTitle(), li.getTitle());
        double descSim = fieldSimilarity(fi.getDescription(), li.getDescription());
        double locSim = fieldSimilarity(fi.getLocation(), li.getLocation());

        return TITLE_WEIGHT * titleSim
                + DESCRIPTION_WEIGHT * descSim
                + LOCATION_WEIGHT * locSim;
    }

    /**
     * 为指定寻物启事查找最相似的失物信息。
     * @param findItemId 寻物启事 ID
     * @param limit 返回数量上限
     * @return 按相似度降序排列的匹配结果列表
     * @throws RuntimeException 若寻物启事不存在
     */
    public List<MatchResult> findMatches(Long findItemId, int limit) {
        FindItem findItem = findItemService.findById(findItemId);
        List<LostItem> allLostItems = lostItemRepository.findAll();

        return allLostItems.stream()
                .map(li -> new MatchResult(li, calculateScore(findItem, li)))
                .sorted(Comparator.comparingDouble(MatchResult::getScore).reversed())
                .limit(limit)
                .collect(Collectors.toUnmodifiableList());
    }
}
