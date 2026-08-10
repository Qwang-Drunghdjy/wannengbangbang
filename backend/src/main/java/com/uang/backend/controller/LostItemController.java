package com.uang.backend.controller;

import com.uang.backend.dto.Result;
import com.uang.backend.entity.LostItem;
import com.uang.backend.service.LostItemService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

/**
 * 失物信息 RESTful API 控制器
 */
@RestController
@RequestMapping("/api/v1/lost-items")
public class LostItemController {

    private final LostItemService service;

    public LostItemController(LostItemService service) {
        this.service = service;
    }

    /**
     * 发布新的失物信息
     * POST /api/v1/lost-items
     * @param item 失物信息（JSON body）
     * @return 保存后的失物
     */
    @PostMapping
    public Result<LostItem> create(@RequestBody LostItem item) {
        LostItem saved = service.create(item);
        return Result.success(saved);
    }

    /**
     * 获取失物列表（分页 + 标题搜索）
     * GET /api/v1/lost-items?title=xxx&page=0&size=10
     * @param title 标题关键词（可选）
     * @param pageable 分页参数
     * @return 分页结果
     */
    @GetMapping
    public Result<Page<LostItem>> list(
            @RequestParam(required = false) String title,
            Pageable pageable) {
        Page<LostItem> page = service.findAll(title, pageable);
        return Result.success(page);
    }

    /**
     * 获取特定失物的详细信息
     * GET /api/v1/lost-items/{id}
     * @param id 失物ID
     * @return 失物详情
     */
    @GetMapping("/{id}")
    public Result<LostItem> getById(@PathVariable Long id) {
        LostItem item = service.findById(id);
        return Result.success(item);
    }
}
