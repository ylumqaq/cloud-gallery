package com.ylum.cloudgallery.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.HeaderParameter;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j（OpenAPI3）接口文档配置。
 *
 * <p>访问地址：{@code http://localhost:8123/api/doc.html}（受 context-path /api 影响）。</p>
 */
@Configuration
public class Knife4jConfig {

    /**
     * 文档基本信息。
     */
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("云图库后端接口文档")
                        .description("云图库后端 API，基础路径 /api，统一返回 BaseResponse 结构")
                        .version("1.0.0"));
    }

    /**
     * 为所有接口注入全局请求头参数 satoken，便于在 Knife4j 中直接测试需要登录的接口。
     */
    @Bean
    public OperationCustomizer globalHeaderCustomizer() {
        return (operation, handlerMethod) -> operation.addParametersItem(
                new HeaderParameter()
                        .name("satoken")
                        .required(false)
                        .description("登录凭证（Sa-Token），登录接口返回后填入此处")
                        .schema(new StringSchema()));
    }
}
