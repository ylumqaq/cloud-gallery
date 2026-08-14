package com.ylum.cloudgallery.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cache.support.AbstractValueAdaptingCache;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * 两级缓存实现：一级本地 Caffeine，二级 Redis。
 *
 * <p>读取顺序为「Caffeine 命中优先，未命中再查 Redis 并回填本地」；
 * 写入时同时更新两级缓存；失效时同时删除两级缓存。</p>
 */
public class TwoLevelCache extends AbstractValueAdaptingCache {

    /** Redis 二级缓存默认过期时间 */
    private static final Duration REDIS_TTL = Duration.ofMinutes(30);

    /** 缓存名称（对应 @Cacheable 的 cacheNames） */
    private final String name;

    /** 本地一级缓存（Caffeine） */
    private final com.github.benmanes.caffeine.cache.Cache<Object, Object> caffeine;

    /** Redis 二级缓存客户端 */
    private final StringRedisTemplate redisTemplate;

    /** JSON 序列化器（用于 Redis 值的读写） */
    private final ObjectMapper objectMapper;

    /**
     * 构造两级缓存实例。
     *
     * @param name           缓存名称
     * @param caffeine       本地 Caffeine 缓存
     * @param redisTemplate  Redis 客户端
     * @param objectMapper   JSON 序列化器
     */
    public TwoLevelCache(String name,
                         com.github.benmanes.caffeine.cache.Cache<Object, Object> caffeine,
                         StringRedisTemplate redisTemplate,
                         ObjectMapper objectMapper) {
        // allowNullValues=false：不缓存 null 值，@Cacheable 返回 null 时 Spring 会跳过缓存
        super(false);
        this.name = name;
        this.caffeine = caffeine;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * 返回缓存名称。
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * 返回底层本地缓存，供诊断使用。
     */
    @Override
    public Object getNativeCache() {
        return caffeine;
    }

    /**
     * 读取缓存：先查本地 Caffeine，未命中再查 Redis，Redis 命中后回填本地。
     */
    @Override
    protected Object lookup(Object key) {
        String localKey = buildLocalKey(key);
        Object value = caffeine.getIfPresent(localKey);
        if (value != null) {
            return value;
        }

        String redisKey = buildRedisKey(key);
        String json = redisTemplate.opsForValue().get(redisKey);
        if (json == null) {
            return null;
        }
        try {
            Object parsed = objectMapper.readValue(json, Object.class);
            if (parsed != null) {
                caffeine.put(localKey, parsed);
            }
            return parsed;
        } catch (Exception e) {
            // Redis 值反序列化失败按未命中处理
            return null;
        }
    }

    /**
     * 读取缓存，未命中时调用 valueLoader 加载并回填缓存。
     */
    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
        ValueWrapper wrapper = get(key);
        if (wrapper != null) {
            @SuppressWarnings("unchecked")
            T value = (T) wrapper.get();
            return value;
        }
        try {
            T value = valueLoader.call();
            put(key, value);
            return value;
        } catch (Exception e) {
            throw new ValueRetrievalException(key, valueLoader, e);
        }
    }

    /**
     * 写入缓存：同时更新本地与 Redis 两级缓存。
     */
    @Override
    public void put(Object key, Object value) {
        caffeine.put(buildLocalKey(key), value);
        try {
            String json = objectMapper.writeValueAsString(value);
            redisTemplate.opsForValue().set(buildRedisKey(key), json, REDIS_TTL);
        } catch (Exception e) {
            // Redis 写入失败不影响本地缓存命中
        }
    }

    /**
     * 删除缓存：同时删除本地与 Redis 两级缓存。
     */
    @Override
    public void evict(Object key) {
        caffeine.invalidate(buildLocalKey(key));
        redisTemplate.delete(buildRedisKey(key));
    }

    /**
     * 清空当前缓存名称下的全部本地与 Redis 缓存。
     */
    @Override
    public void clear() {
        caffeine.invalidateAll();
        Set<String> keys = redisTemplate.keys(buildRedisPrefix() + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    /**
     * 失效全部缓存（等价于 clear）。
     */
    @Override
    public boolean invalidate() {
        clear();
        return true;
    }

    /**
     * 构建本地缓存 key：{@code cacheName:key}。
     */
    private String buildLocalKey(Object key) {
        return name + ":" + key;
    }

    /**
     * 构建 Redis key 前缀：{@code cache:cacheName:}。
     */
    private String buildRedisPrefix() {
        return "cache:" + name + ":";
    }

    /**
     * 构建 Redis 完整 key：{@code cache:cacheName:key}。
     */
    private String buildRedisKey(Object key) {
        return buildRedisPrefix() + key;
    }
}
