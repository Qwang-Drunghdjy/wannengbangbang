package com.uang.backend.service;

import com.uang.backend.entity.FindItem;
import com.uang.backend.entity.User;
import com.uang.backend.exception.ForbiddenException;
import com.uang.backend.repository.FindItemRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 寻物信息业务逻辑层
 */
@Service
public class FindItemService {

    private final FindItemRepository repository;
    private final UserService userService;

    public FindItemService(FindItemRepository repository, UserService userService) {
        this.repository = repository;
        this.userService = userService;
    }

    /**
     * 发布新的寻物信息
     * @param item 寻物实体（id 和 createTime 由服务端设置）
     * @param userId 发布者用户 ID（来自 JWT）
     * @return 保存后的寻物实体
     */
    public FindItem create(FindItem item, Long userId) {
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
     * 更新寻物信息的认领状态（仅发布者本人可操作）。
     * @param id      寻物 ID
     * @param userId  当前登录用户 ID（来自 JWT）
     * @param claimed 目标认领状态
     * @return 更新后的寻物实体
     * @throws ForbiddenException 非发布者本人时抛出
     */
    public FindItem updateClaimed(Long id, Long userId, boolean claimed) {
        FindItem item = findById(id);
        if (!item.getUser().getId().equals(userId)) {
            throw new ForbiddenException("无权修改该寻物信息的认领状态");
        }
        item.setClaimed(claimed);
        return repository.save(item);
    }

    /**
     * 根据 ID 查询寻物详情
     * @param id 主键
     * @return 寻物实体
     * @throws RuntimeException 未找到时抛出
     */
    public FindItem findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("寻物信息不存在，id: " + id));
    }

    /**
     * 分页查询寻物列表，支持标题模糊搜索与按发布者过滤
     * @param title 搜索关键词（可选）
     * @param userId 发布者用户 ID（仅查看我的时传入，可选）
     * @param pageable 分页参数
     * @return 分页结果
     */
    public Page<FindItem> findAll(String title, Long userId, Pageable pageable) {
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
