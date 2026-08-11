package com.uang.backend.controller;

import com.uang.backend.dto.MatchResult;
import com.uang.backend.dto.Result;
import com.uang.backend.service.MatchingService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 智能匹配 RESTful API 控制器
 */
@RestController
@RequestMapping("/api/v1")
public class MatchController {

    private final MatchingService matchingService;

    public MatchController(MatchingService matchingService) {
        this.matchingService = matchingService;
    }

    /**
     * 为指定寻物启事查找最相似的失物信息。
     * GET /api/v1/find-items/{id}/matches?limit=3
     * @param id    寻物启事 ID
     * @param limit 返回数量上限（默认 3）
     * @return 匹配结果列表
     */
    @GetMapping("/find-items/{id}/matches")
    public Result<List<MatchResult>> findMatches(
            @PathVariable Long id,
            @RequestParam(defaultValue = "3") int limit) {
        List<MatchResult> matches = matchingService.findMatches(id, limit);
        return Result.success(matches);
    }
}
