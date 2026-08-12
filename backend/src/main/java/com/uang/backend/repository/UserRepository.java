package com.uang.backend.repository;

import com.uang.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 用户数据访问层
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 根据手机号查询用户
     * @param phone 手机号
     * @return 用户（可能为空）
     */
    Optional<User> findByPhone(String phone);
}
