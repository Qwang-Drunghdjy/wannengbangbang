package com.uang.backend.service;

import com.uang.backend.entity.FindItem;
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

    public FindItemService(FindItemRepository repository) {
        this.repository = repository;
    }

    /**
     * 发布新的寻物信息
     * @param item 寻物实体（id 和 createTime 由服务端设置）
     * @return 保存后的寻物实体
     */
    public FindItem create(FindItem item) {
        item.setId(null);
        item.setCreateTime(LocalDateTime.now());
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
     * 分页查询寻物列表，支持标题模糊搜索
     * @param title 搜索关键词（可选）
     * @param pageable 分页参数
     * @return 分页结果
     */
    public Page<FindItem> findAll(String title, Pageable pageable) {
        if (StringUtils.hasText(title)) {
            return repository.findByTitleContaining(title, pageable);
        }
        return repository.findAll(pageable);
    }
}
