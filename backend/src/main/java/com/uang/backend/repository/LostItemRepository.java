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

    /**
     * 根据发布者用户 ID 分页查询（仅查看我的）
     * @param userId 发布者用户 ID
     * @param pageable 分页参数
     * @return 分页结果
     */
    Page<LostItem> findByUserId(Long userId, Pageable pageable);

    /**
     * 根据发布者用户 ID + 标题模糊搜索（分页）
     * @param userId 发布者用户 ID
     * @param title 搜索关键词
     * @param pageable 分页参数
     * @return 分页结果
     */
    Page<LostItem> findByUserIdAndTitleContaining(Long userId, String title, Pageable pageable);
}
