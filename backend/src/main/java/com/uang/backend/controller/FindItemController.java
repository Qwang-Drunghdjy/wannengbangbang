package com.uang.backend.controller;

import com.uang.backend.dto.Result;
import com.uang.backend.entity.FindItem;
import com.uang.backend.service.FindItemService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

/**
 * 寻物信息 RESTful API 控制器
 */
@RestController
@RequestMapping("/api/v1/find-items")
public class FindItemController {

    private final FindItemService service;

    public FindItemController(FindItemService service) {
        this.service = service;
    }

    /**
     * 发布新的寻物信息
     * POST /api/v1/find-items
     * @param item 寻物信息（JSON body）
     * @return 保存后的寻物
     */
    @PostMapping
    public Result<FindItem> create(@RequestBody FindItem item) {
        FindItem saved = service.create(item);
        return Result.success(saved);
    }

    /**
     * 获取寻物列表（分页 + 标题搜索）
     * GET /api/v1/find-items?title=xxx&page=0&size=10
     * @param title 标题关键词（可选）
     * @param pageable 分页参数
     * @return 分页结果
     */
    @GetMapping
    public Result<Page<FindItem>> list(
            @RequestParam(required = false) String title,
            Pageable pageable) {
        Page<FindItem> page = service.findAll(title, pageable);
        return Result.success(page);
    }

    /**
     * 获取特定寻物的详细信息
     * GET /api/v1/find-items/{id}
     * @param id 寻物ID
     * @return 寻物详情
     */
    @GetMapping("/{id}")
    public Result<FindItem> getById(@PathVariable Long id) {
        FindItem item = service.findById(id);
        return Result.success(item);
    }
}
