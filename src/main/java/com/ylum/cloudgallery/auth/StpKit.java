package com.ylum.cloudgallery.auth;

import cn.dev33.satoken.stp.StpLogic;

/**
 * Sa-Token 多账号体系门面（Kit 模式）。
 *
 * <p>统一管理项目中所有 {@link StpLogic} 账号体系：
 * 用户登录沿用 Sa-Token 默认账号体系（{@code StpUtil}），
 * 空间权限使用独立的 {@code SPACE} 账号体系标识。</p>
 */
public class StpKit {

    /** 空间账号体系标识 */
    public static final String SPACE_TYPE = "space";

    /** 空间账号体系 StpLogic（用于空间维度权限的动态加载） */
    public static final StpLogic SPACE = new StpLogic(SPACE_TYPE);
}
