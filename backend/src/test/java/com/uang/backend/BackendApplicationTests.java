package com.uang.backend;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 全量上下文测试——需要 MySQL 连接，日常开发时禁用。
 * 部署前取消 @Disabled 做集成验证。
 */
@SpringBootTest
@Disabled("需要 MySQL 连接，日常开发时跳过")
class BackendApplicationTests {

    @Test
    void contextLoads() {
    }

}
