package com.ylum.cloudgallery;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 云图库后端启动类
 *
 * <p>扫描 com.ylum.cloudgallery.mapper 包下的 MyBatis Mapper 接口，
 * 后续阶段新增的 Mapper 均放置在该包下即可被自动扫描。</p>
 */
@SpringBootApplication
@MapperScan("com.ylum.cloudgallery.mapper")
public class YuPictureApplication {

    public static void main(String[] args) {
        SpringApplication.run(YuPictureApplication.class, args);
    }
}
