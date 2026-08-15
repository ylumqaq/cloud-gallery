package com.ylum.cloudgallery.service;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * 缓存测试服务：用于演示 Caffeine + Redis 两级缓存。
 *
 * <p>{@code @Cacheable} 标记的方法首次调用会执行方法体并将结果写入缓存，
 * 后续调用直接返回缓存值，方法体不再执行。</p>
 */
@Service
public class CacheTestService {

    /**
     * 获取带时间戳的测试值。
     *
     * <p>首次调用生成新的时间戳，命中缓存后返回首次生成的旧值，
     * 通过观察返回时间戳是否变化即可判断缓存是否生效。</p>
     *
     * @return 带时间戳的测试值
     */
    @Cacheable(cacheNames = "cache:test", key = "'value'")
    public String getTestValue() {
        return "value_" + System.currentTimeMillis();
    }
}
