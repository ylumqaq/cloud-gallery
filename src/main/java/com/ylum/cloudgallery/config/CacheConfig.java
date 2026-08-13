package com.ylum.cloudgallery.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * 缓存配置：启用 Spring Cache 抽象，注册「Caffeine + Redis」两级缓存管理器。
 *
 * <p>一级缓存为本地 Caffeine（进程内，命中最快），二级缓存为 Redis（跨实例共享），
 * 由 {@link TwoLevelCacheManager} 组合实现。</p>
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * 注册两级缓存管理器，供 {@code @Cacheable} 等注解使用。
     *
     * @param stringRedisTemplate Redis 客户端
     * @param objectMapper        JSON 序列化器
     * @return 两级缓存管理器
     */
    @Bean
    public CacheManager cacheManager(StringRedisTemplate stringRedisTemplate, ObjectMapper objectMapper) {
        return new TwoLevelCacheManager(stringRedisTemplate, objectMapper);
    }
}
