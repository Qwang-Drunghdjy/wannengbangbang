package com.uang.backend.service;

import com.uang.backend.dto.MatchResult;
import com.uang.backend.entity.FindItem;
import com.uang.backend.entity.LostItem;
import com.uang.backend.repository.LostItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.byLessThan;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MatchingServiceTest {

    @Mock
    private FindItemService findItemService;

    @Mock
    private LostItemRepository lostItemRepository;

    private MatchingService matchingService;

    @BeforeEach
    void setUp() {
        matchingService = new MatchingService(findItemService, lostItemRepository);
    }

    // ── tokenize ──────────────────────────────────────────────

    @Test
    void tokenize_shouldReturnWordSet() {
        Set<String> tokens = matchingService.tokenize("黑色钱包");
        assertThat(tokens).isNotEmpty();
        // HanLP 至少应包含 "钱包"
        assertThat(tokens).anyMatch(word -> word.contains("钱包"));
    }

    @Test
    void tokenize_shouldReturnEmptySetForNull() {
        Set<String> tokens = matchingService.tokenize(null);
        assertThat(tokens).isEmpty();
    }

    @Test
    void tokenize_shouldReturnEmptySetForBlank() {
        Set<String> tokens = matchingService.tokenize("   ");
        assertThat(tokens).isEmpty();
    }

    // ── jaccard ───────────────────────────────────────────────

    @Test
    void jaccard_identicalSets_shouldReturnOne() {
        Set<String> a = Set.of("钱包", "黑色");
        double result = matchingService.jaccard(a, a);
        assertThat(result).isCloseTo(1.0, byLessThan(0.001));
    }

    @Test
    void jaccard_disjointSets_shouldReturnZero() {
        Set<String> a = Set.of("钱包", "黑色");
        Set<String> b = Set.of("手机", "白色");
        double result = matchingService.jaccard(a, b);
        assertThat(result).isCloseTo(0.0, byLessThan(0.001));
    }

    @Test
    void jaccard_partialOverlap_shouldReturnCorrectValue() {
        Set<String> a = Set.of("钱包", "黑色", "皮夹");
        Set<String> b = Set.of("钱包", "棕色", "皮夹");
        // |A ∩ B| = 2 ("钱包","皮夹"), |A ∪ B| = 4 ("钱包","黑色","皮夹","棕色")
        double result = matchingService.jaccard(a, b);
        assertThat(result).isCloseTo(2.0 / 4.0, byLessThan(0.001));
    }

    @Test
    void jaccard_bothEmpty_shouldReturnZero() {
        double result = matchingService.jaccard(Set.of(), Set.of());
        assertThat(result).isCloseTo(0.0, byLessThan(0.001));
    }

    // ── fieldSimilarity ───────────────────────────────────────

    @Test
    void fieldSimilarity_bothValid_shouldReturnPositiveScore() {
        double score = matchingService.fieldSimilarity("黑色钱包", "黑色皮夹");
        // "黑色" 共有 → 相似度 > 0
        assertThat(score).isGreaterThan(0.0);
    }

    @Test
    void fieldSimilarity_firstNull_shouldReturnZero() {
        double score = matchingService.fieldSimilarity(null, "黑色钱包");
        assertThat(score).isCloseTo(0.0, byLessThan(0.001));
    }

    @Test
    void fieldSimilarity_secondBlank_shouldReturnZero() {
        double score = matchingService.fieldSimilarity("黑色钱包", "   ");
        assertThat(score).isCloseTo(0.0, byLessThan(0.001));
    }

    @Test
    void fieldSimilarity_bothEmpty_shouldReturnZero() {
        double score = matchingService.fieldSimilarity("", "");
        assertThat(score).isCloseTo(0.0, byLessThan(0.001));
    }

    // ── calculateScore ────────────────────────────────────────

    @Test
    void calculateScore_allFieldsMatch_shouldReturnOne() {
        FindItem fi = new FindItem();
        fi.setTitle("黑色钱包");
        fi.setDescription("在食堂丢失");
        fi.setLocation("一食堂");

        LostItem li = new LostItem();
        li.setTitle("黑色钱包");
        li.setDescription("在食堂丢失");
        li.setLocation("一食堂");

        double score = matchingService.calculateScore(fi, li);

        // 三个字段都完全一致 → 得分接近 1.0
        assertThat(score).isGreaterThan(0.9);
    }

    @Test
    void calculateScore_emptyFields_shouldNotAffectScore() {
        FindItem fi = new FindItem();
        fi.setTitle("黑色钱包");
        fi.setDescription(null);
        fi.setLocation(null);

        LostItem li = new LostItem();
        li.setTitle("黑色钱包");
        li.setDescription("某个描述");
        li.setLocation("某地点");

        double score = matchingService.calculateScore(fi, li);

        // title 完全一致 → 0.6，description 和 location 为 0
        assertThat(score).isCloseTo(0.6, byLessThan(0.001));
    }

    // ── findMatches ───────────────────────────────────────────

    @Test
    void findMatches_shouldReturnSortedResults() {
        FindItem fi = new FindItem();
        fi.setId(1L);
        fi.setTitle("黑色钱包");

        LostItem li1 = new LostItem();
        li1.setId(1L);
        li1.setTitle("黑色钱包");

        LostItem li2 = new LostItem();
        li2.setId(2L);
        li2.setTitle("白色手机");

        when(findItemService.findById(1L)).thenReturn(fi);
        when(lostItemRepository.findAll()).thenReturn(List.of(li1, li2));

        List<MatchResult> results = matchingService.findMatches(1L, 3);

        assertThat(results).hasSize(2);
        assertThat(results.get(0).getScore())
                .isGreaterThanOrEqualTo(results.get(1).getScore());
        // 第一条应当是 "黑色钱包"（title 完全匹配）
        assertThat(results.get(0).getLostItem().getTitle()).isEqualTo("黑色钱包");
    }

    @Test
    void findMatches_findItemNotFound_shouldThrow() {
        when(findItemService.findById(999L))
                .thenThrow(new RuntimeException("寻物信息不存在，id: 999"));

        assertThatThrownBy(() -> matchingService.findMatches(999L, 3))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("寻物信息不存在");
    }

    @Test
    void findMatches_noLostItems_shouldReturnEmptyList() {
        FindItem fi = new FindItem();
        fi.setId(1L);
        fi.setTitle("黑色钱包");

        when(findItemService.findById(1L)).thenReturn(fi);
        when(lostItemRepository.findAll()).thenReturn(List.of());

        List<MatchResult> results = matchingService.findMatches(1L, 3);

        assertThat(results).isEmpty();
    }
}
