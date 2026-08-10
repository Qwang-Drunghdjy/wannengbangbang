package com.uang.backend.repository;

import com.uang.backend.entity.LostItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 失物信息数据访问层
 */
@Repository
public interface LostItemRepository extends JpaRepository<LostItem, Long> {

    /**
     * 根据标题模糊搜索（分页）
     * @param title 搜索关键词
     * @param pageable 分页参数
     * @return 分页结果
     */
    Page<LostItem> findByTitleContaining(String title, Pageable pageable);
}
