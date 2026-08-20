package com.uang.backend.controller;

import com.uang.backend.config.AuthInterceptor;
import com.uang.backend.config.JwtUtil;
import com.uang.backend.dto.ClaimRequest;
import com.uang.backend.dto.Result;
import com.uang.backend.entity.LostItem;
import com.uang.backend.exception.UnauthorizedException;
import com.uang.backend.service.LostItemService;
import jakarta.servlet.http.HttpServletRequest;
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
    private final JwtUtil jwtUtil;

    public LostItemController(LostItemService service, JwtUtil jwtUtil) {
        this.service = service;
        this.jwtUtil = jwtUtil;
    }

    /**
     * 发布新的失物信息
     * POST /api/v1/lost-items
     * @param item 失物信息（JSON body）
     * @param request 请求（从中提取拦截器注入的 userId）
     * @return 保存后的失物
     */
    @PostMapping
    public Result<LostItem> create(@RequestBody LostItem item, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(AuthInterceptor.USER_ID_ATTR);
        LostItem saved = service.create(item, userId);
        return Result.success(saved);
    }

    /**
     * 获取失物列表（分页 + 标题搜索 + 仅查看我的）
     * GET /api/v1/lost-items?title=xxx&page=0&size=10&mine=true
     * @param title 标题关键词（可选）
     * @param mine 是否仅查看当前用户发布的（可选，需登录，默认 false）
     * @param request 请求（mine=true 时解析 Bearer token 取 userId）
     * @param pageable 分页参数
     * @return 分页结果
     */
    @GetMapping
    public Result<Page<LostItem>> list(
            @RequestParam(required = false) String title,
            @RequestParam(required = false, defaultValue = "false") Boolean mine,
            HttpServletRequest request,
            Pageable pageable) {
        Long userId = null;
        if (Boolean.TRUE.equals(mine)) {
            userId = AuthInterceptor.extractUserId(request, jwtUtil);
            if (userId == null) {
                throw new UnauthorizedException("未登录或登录已过期");
            }
        }
        Page<LostItem> page = service.findAll(title, userId, pageable);
        return Result.success(page);
    }

    /**
     * 更新认领状态后可编辑字段的更新（仅发布者本人可操作，POST 已被拦截器鉴权，非本人 403）。
     * POST /api/v1/lost-items/{id}
     * @param id      拾物 ID
     * @param request 请求（从中提取拦截器注入的 userId）
     * @param item    编辑后的字段（JSON body）
     * @return 更新后的拾物
     */
    @PostMapping("/{id}")
    public Result<LostItem> update(@PathVariable Long id, HttpServletRequest request,
                                   @RequestBody LostItem item) {
        Long userId = (Long) request.getAttribute(AuthInterceptor.USER_ID_ATTR);
        LostItem saved = service.update(id, userId, item);
        return Result.success(saved);
    }

    /**
     * 更新拾物信息的认领状态（仅发布者本人可操作，POST 已被拦截器鉴权）。
     * POST /api/v1/lost-items/{id}/claim
     * @param id      拾物 ID
     * @param request 请求（从中提取拦截器注入的 userId）
     * @param body    认领状态 {@link ClaimRequest}
     * @return 更新后的拾物
     */
    @PostMapping("/{id}/claim")
    public Result<LostItem> updateClaimed(@PathVariable Long id, HttpServletRequest request,
                                          @RequestBody ClaimRequest body) {
        Long userId = (Long) request.getAttribute(AuthInterceptor.USER_ID_ATTR);
        LostItem saved = service.updateClaimed(id, userId, body.isClaimed());
        return Result.success(saved);
    }

    /**
     * 删除拾物信息（仅发布者本人可操作，POST 已被拦截器鉴权，非本人 403）。
     * POST /api/v1/lost-items/{id}/delete
     * @param id      拾物 ID
     * @param request 请求（从中提取拦截器注入的 userId）
     * @return 删除结果（无数据体）
     */
    @PostMapping("/{id}/delete")
    public Result<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(AuthInterceptor.USER_ID_ATTR);
        service.delete(id, userId);
        return Result.success(null);
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
