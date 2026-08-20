package com.uang.backend.service;

import com.uang.backend.entity.LostItem;
import com.uang.backend.entity.User;
import com.uang.backend.exception.ForbiddenException;
import com.uang.backend.repository.LostItemRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 失物信息业务逻辑层
 */
@Service
public class LostItemService {

    private final LostItemRepository repository;
    private final UserService userService;

    public LostItemService(LostItemRepository repository, UserService userService) {
        this.repository = repository;
        this.userService = userService;
    }

    /**
     * 发布新的失物信息
     * @param item 失物实体（id 和 createTime 由服务端设置）
     * @param userId 发布者用户 ID（来自 JWT）
     * @return 保存后的失物实体
     */
    public LostItem create(LostItem item, Long userId) {
        item.setId(null);
        item.setCreateTime(LocalDateTime.now());
        User user = userService.findById(userId);
        item.setUser(user);
        if (!StringUtils.hasText(item.getContact())) {
            item.setContact(user.getPhone());
        }
        return repository.save(item);
    }

    /**
     * 更新认领状态后可编辑字段的更新（仅发布者本人可操作）。
     * 保留 id / createTime / user / claimed，仅覆盖 title / description / location / contact / imageUrl；
     * contact 为空时取发布者手机号（与 create 一致）。
     * @param id      拾物 ID
     * @param userId  当前登录用户 ID（来自 JWT）
     * @param item    编辑后的字段（来自请求体）
     * @return 更新后的拾物实体
     * @throws ForbiddenException 非发布者本人时抛出
     */
    public LostItem update(Long id, Long userId, LostItem item) {
        LostItem existing = findById(id);
        if (!existing.getUser().getId().equals(userId)) {
            throw new ForbiddenException("无权修改该拾物信息");
        }
        existing.setTitle(item.getTitle());
        existing.setDescription(item.getDescription());
        existing.setLocation(item.getLocation());
        existing.setImageUrl(item.getImageUrl());
        if (StringUtils.hasText(item.getContact())) {
            existing.setContact(item.getContact());
        } else {
            existing.setContact(existing.getUser().getPhone());
        }
        return repository.save(existing);
    }

    /**
     * 删除拾物信息（仅发布者本人可操作）。
     * @param id      拾物 ID
     * @param userId  当前登录用户 ID（来自 JWT）
     * @throws ForbiddenException 非发布者本人时抛出
     */
    public void delete(Long id, Long userId) {
        LostItem existing = findById(id);
        if (!existing.getUser().getId().equals(userId)) {
            throw new ForbiddenException("无权删除该拾物信息");
        }
        repository.delete(existing);
    }

    /**
     * 更新拾物信息的认领状态（仅发布者本人可操作）。
     * @param id      拾物 ID
     * @param userId  当前登录用户 ID（来自 JWT）
     * @param claimed 目标认领状态
     * @return 更新后的拾物实体
     * @throws ForbiddenException 非发布者本人时抛出
     */
    public LostItem updateClaimed(Long id, Long userId, boolean claimed) {
        LostItem item = findById(id);
        if (!item.getUser().getId().equals(userId)) {
            throw new ForbiddenException("无权修改该拾物信息的认领状态");
        }
        item.setClaimed(claimed);
        return repository.save(item);
    }

    /**
     * 根据 ID 查询拾物详情
     * @param id 主键
     * @return 拾物实体
     * @throws RuntimeException 未找到时抛出
     */
    public LostItem findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("失物信息不存在，id: " + id));
    }

    /**
     * 分页查询失物列表，支持标题模糊搜索与按发布者过滤
     * @param title 搜索关键词（可选）
     * @param userId 发布者用户 ID（仅查看我的时传入，可选）
     * @param pageable 分页参数
     * @return 分页结果
     */
    public Page<LostItem> findAll(String title, Long userId, Pageable pageable) {
        if (userId != null && StringUtils.hasText(title)) {
            return repository.findByUserIdAndTitleContaining(userId, title, pageable);
        }
        if (userId != null) {
            return repository.findByUserId(userId, pageable);
        }
        if (StringUtils.hasText(title)) {
            return repository.findByTitleContaining(title, pageable);
        }
        return repository.findAll(pageable);
    }
}
