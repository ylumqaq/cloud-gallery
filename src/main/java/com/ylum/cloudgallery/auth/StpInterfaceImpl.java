package com.ylum.cloudgallery.auth;

import cn.dev33.satoken.stp.StpInterface;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * Sa-Token 动态权限加载实现。
 *
 * <p>实现 {@link StpInterface}，为 Sa-Token 提供按账号体系（loginType）
 * 动态返回权限码 / 角色的能力。空间权限（loginType = {@link StpKit#SPACE_TYPE}）
 * 通过 {@link SpaceUserAuthManager} 从请求上下文解析 spaceId 后动态计算。</p>
 */
@Component
public class StpInterfaceImpl implements StpInterface {

    @Resource
    private SpaceUserAuthManager spaceUserAuthManager;

    /**
     * 返回指定账号在指定账号体系下的权限码列表。
     *
     * @param loginId   登录账号 id（此处为 userId）
     * @param loginType 账号体系标识
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        if (StpKit.SPACE_TYPE.equals(loginType)) {
            Long userId = Long.valueOf(loginId.toString());
            Long spaceId = SpaceUserAuthContext.getSpaceId();
            return spaceUserAuthManager.getPermissionList(spaceId, userId);
        }
        return Collections.emptyList();
    }

    /**
     * 返回指定账号在指定账号体系下的角色列表。
     *
     * @param loginId   登录账号 id（此处为 userId）
     * @param loginType 账号体系标识
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        if (StpKit.SPACE_TYPE.equals(loginType)) {
            Long userId = Long.valueOf(loginId.toString());
            Long spaceId = SpaceUserAuthContext.getSpaceId();
            String role = spaceUserAuthManager.getRole(spaceId, userId);
            return role == null ? Collections.emptyList() : Collections.singletonList(role);
        }
        return Collections.emptyList();
    }
}
