package com.ylum.cloudgallery.auth;

import cn.dev33.satoken.stp.StpUtil;
import com.ylum.cloudgallery.common.BusinessException;
import com.ylum.cloudgallery.common.ErrorCode;
import jakarta.annotation.Resource;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.List;

/**
 * 空间权限校验切面。
 *
 * <p>拦截标注了 {@link SaSpaceCheckPermission} 的方法，解析空间 ID 与当前用户，
 * 调用 {@link SpaceUserAuthManager} 获取权限码列表并校验，未通过时抛出无权限异常。</p>
 */
@Aspect
@Component
public class SpaceUserAuthAspect {

    @Resource
    private SpaceUserAuthManager spaceUserAuthManager;

    /**
     * 环绕通知：校验 {@link SaSpaceCheckPermission} 注解声明的空间权限码。
     */
    @Around("@annotation(saSpaceCheckPermission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint, SaSpaceCheckPermission saSpaceCheckPermission) throws Throwable {
        // 当前登录用户（未登录由 Sa-Token 抛出未登录异常）
        long userId = StpUtil.getLoginIdAsLong();

        // 解析空间 ID：优先从方法参数（@RequestBody）获取，其次从请求参数获取
        Long spaceId = resolveSpaceId(joinPoint.getArgs());

        // 动态加载当前用户在空间下的权限码列表
        List<String> permissionList = spaceUserAuthManager.getPermissionList(spaceId, userId);

        // 需同时拥有注解声明的全部权限码（AND 模式）
        for (String permission : saSpaceCheckPermission.value()) {
            if (!permissionList.contains(permission)) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无空间权限");
            }
        }
        return joinPoint.proceed();
    }

    /**
     * 从方法参数中解析 spaceId。
     *
     * <p>POST 请求的 spaceId 位于 @RequestBody 参数对象中，需反射调用
     * {@code getSpaceId()} 获取；无法从参数解析时回退到请求参数。</p>
     */
    private Long resolveSpaceId(Object[] args) {
        for (Object arg : args) {
            if (arg == null) {
                continue;
            }
            Long spaceId = getSpaceIdByReflect(arg);
            if (spaceId != null) {
                return spaceId;
            }
        }
        return SpaceUserAuthContext.getSpaceId();
    }

    /**
     * 反射调用对象的 {@code getSpaceId()} 方法获取空间 ID。
     */
    private Long getSpaceIdByReflect(Object obj) {
        try {
            Method getter = obj.getClass().getMethod("getSpaceId");
            Object value = getter.invoke(obj);
            if (value instanceof Long id) {
                return id;
            }
        } catch (NoSuchMethodException ignored) {
            // 对象不含 getSpaceId 方法，忽略
        } catch (Exception ignored) {
            // 反射调用异常，忽略（后续回退到请求参数解析）
        }
        return null;
    }
}
