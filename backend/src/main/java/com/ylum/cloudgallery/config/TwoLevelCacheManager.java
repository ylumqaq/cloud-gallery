package com.ylum.cloudgallery.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 两级缓存管理器：为每个缓存名称创建 {@link TwoLevelCache}。
 *
 * <p>本地 Caffeine 缓存全局共享一份实例，各缓存名称通过 key 前缀隔离；
 * Redis 通过 {@code cache:cacheName:} 前缀隔离。</p>
 */
public class TwoLevelCacheManager implements CacheManager {

    /** 本地一级缓存过期时间 */
    private static final Duration LOCAL_TTL = Duration.ofMinutes(10);

    /** 本地一级缓存最大条目数 */
    private static final long LOCAL_MAX_SIZE = 1000;

    /** 本地 Caffeine 缓存（全局共享） */
    private final com.github.benmanes.caffeine.cache.Cache<Object, Object> caffeine;

    /** Redis 二级缓存客户端 */
    private final StringRedisTemplate redisTemplate;

    /** JSON 序列化器 */
    private final ObjectMapper objectMapper;

    /** 缓存名称 -> Cache 实例 */
    private final Map<String, Cache> cacheMap = new ConcurrentHashMap<>();

    /**
     * 构造两级缓存管理器。
     *
     * @param redisTemplate Redis 客户端
     * @param objectMapper  JSON 序列化器
     */
    public TwoLevelCacheManager(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.caffeine = Caffeine.newBuilder()
                .maximumSize(LOCAL_MAX_SIZE)
                .expireAfterWrite(LOCAL_TTL)
                .build();
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * 获取（或创建）指定名称的缓存实例。
     */
    @Override
    public Cache getCache(String name) {
        return cacheMap.computeIfAbsent(name,
                n -> new TwoLevelCache(n, caffeine, redisTemplate, objectMapper));
    }

    /**
     * 返回所有已创建的缓存名称。
     */
    @Override
    public Collection<String> getCacheNames() {
        return cacheMap.keySet();
    }
}
