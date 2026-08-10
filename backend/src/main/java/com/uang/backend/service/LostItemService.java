package com.uang.backend.service;

import com.uang.backend.entity.LostItem;
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

    public LostItemService(LostItemRepository repository) {
        this.repository = repository;
    }

    /**
     * 发布新的失物信息
     * @param item 失物实体（id 和 createTime 由服务端设置）
     * @return 保存后的失物实体
     */
    public LostItem create(LostItem item) {
        item.setId(null);
        item.setCreateTime(LocalDateTime.now());
        return repository.save(item);
    }

    /**
     * 根据 ID 查询失物详情
     * @param id 主键
     * @return 失物实体
     * @throws RuntimeException 未找到时抛出
     */
    public LostItem findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("失物信息不存在，id: " + id));
    }

    /**
     * 分页查询失物列表，支持标题模糊搜索
     * @param title 搜索关键词（可选）
     * @param pageable 分页参数
     * @return 分页结果
     */
    public Page<LostItem> findAll(String title, Pageable pageable) {
        if (StringUtils.hasText(title)) {
            return repository.findByTitleContaining(title, pageable);
        }
        return repository.findAll(pageable);
    }
}
