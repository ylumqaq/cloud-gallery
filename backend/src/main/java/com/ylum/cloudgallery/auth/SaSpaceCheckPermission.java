package com.ylum.cloudgallery.auth;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 空间权限校验注解。
 *
 * <p>标注在 Controller 方法上，用于校验当前登录用户是否拥有指定空间权限码。
 * 由 {@link SpaceUserAuthAspect} 拦截并完成校验，未拥有任一权限码时抛出无权限异常。</p>
 *
 * <p>示例：{@code @SaSpaceCheckPermission(SpaceUserConstant.PERMISSION_SPACE_USER_MANAGE)}</p>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface SaSpaceCheckPermission {

    /** 需要校验的权限码列表（AND 模式，需全部拥有） */
    String[] value();
}
