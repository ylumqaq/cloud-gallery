package com.ylum.cloudgallery.controller;

import com.ylum.cloudgallery.common.BaseResponse;
import com.ylum.cloudgallery.common.ResultUtils;
import com.ylum.cloudgallery.service.CacheTestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 缓存测试控制器：演示 Caffeine + Redis 两级缓存。
 *
 * <p>接口前缀为 {@code /api/cache}。连续调用 {@code /test} 返回相同时间戳
 * 即说明缓存命中（首次调用后，方法体不再执行）。</p>
 */
@Tag(name = "缓存测试", description = "演示 Caffeine + Redis 两级缓存")
@RestController
@RequestMapping("/cache")
public class CacheController {

    private final CacheTestService cacheTestService;

    public CacheController(CacheTestService cacheTestService) {
        this.cacheTestService = cacheTestService;
    }

    /**
     * 缓存测试接口（无权限限制）。
     */
    @Operation(summary = "缓存测试")
    @GetMapping("/test")
    public BaseResponse<String> testCache() {
        return ResultUtils.success(cacheTestService.getTestValue());
    }
}
