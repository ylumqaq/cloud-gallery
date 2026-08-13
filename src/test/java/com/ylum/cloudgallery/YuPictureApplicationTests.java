package com.ylum.cloudgallery;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 启动冒烟测试：验证 Spring 应用上下文能够正常加载。
 */
@SpringBootTest
class YuPictureApplicationTests {

    /**
     * 上下文加载测试：若启动类、依赖与配置正确，上下文可成功加载。
     */
    @Test
    void contextLoads() {
        // 空方法体即代表验证通过：只要 Spring 上下文能启动成功，测试即通过
    }
}
