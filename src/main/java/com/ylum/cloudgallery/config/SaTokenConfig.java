package com.ylum.cloudgallery.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token 配置：注册拦截器，开启注解式鉴权（{@code @SaCheckLogin} 等）。
 *
 * <p>注册 / 登录接口无需登录，其余需登录的接口通过在方法上标注注解实现拦截。</p>
 */
@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 无参 SaInterceptor 仅开启注解式鉴权，不配置路由级拦截
        registry.addInterceptor(new SaInterceptor()).addPathPatterns("/**");
    }
}
