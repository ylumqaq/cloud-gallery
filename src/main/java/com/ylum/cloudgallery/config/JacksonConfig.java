package com.ylum.cloudgallery.config;

import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Jackson 序列化配置。
 *
 * <p>将 {@link Long} 与基本类型 {@code long} 统一序列化为 JSON 字符串，避免雪花算法生成的
 * 19 位 ID 在 JavaScript 中因超出安全整数范围（{@code Number.MAX_SAFE_INTEGER} = 9007199254740991）
 * 而发生精度丢失。前端拿到的是字符串形式 ID，回传时同样以字符串提交，Jackson 会自动反序列化为 Long。</p>
 */
@Configuration
public class JacksonConfig {

    /**
     * 注册 Long / long 到字符串的序列化器。
     *
     * @return Jackson 构建器定制器，作用于 Spring MVC 请求 / 响应的 ObjectMapper
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer longToStringCustomizer() {
        return builder -> {
            // Long 包装类型 -> 字符串
            builder.serializerByType(Long.class, ToStringSerializer.instance);
            // long 基本类型 -> 字符串
            builder.serializerByType(Long.TYPE, ToStringSerializer.instance);
        };
    }
}
