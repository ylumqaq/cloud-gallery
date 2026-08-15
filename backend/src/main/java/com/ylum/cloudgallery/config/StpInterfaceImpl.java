package com.ylum.cloudgallery.config;

import cn.dev33.satoken.stp.StpInterface;
import com.ylum.cloudgallery.mapper.UserMapper;
import com.ylum.cloudgallery.model.entity.User;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Sa-Token 角色 / 权限提供器实现。
 *
 * <p>Sa-Token 的注解式鉴权（{@code @SaCheckRole}、{@code @SaCheckPermission}）依赖本类
 * 提供当前登录账号的角色与权限列表。登录时以用户 ID 作为 loginId
 * （见 {@code UserServiceImpl#userLogin}），因此这里按 loginId 从 user 表查询角色。</p>
 *
 * <p>本项目无全局权限码体系（空间级权限由 {@code SpaceUserAuthManager} 单独管理），
 * 因此权限列表返回空列表。</p>
 */
@Component
public class StpInterfaceImpl implements StpInterface {

    @Resource
    private UserMapper userMapper;

    /**
     * 返回当前登录账号的角色列表。
     *
     * @param loginId   登录 ID（即用户 ID）
     * @param loginType 登录类型
     * @return 用户角色列表（单角色，如 user / admin / super_admin）
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        // loginId 在登录时以 Long 类型写入，序列化后可能以字符串返回，统一转成 long
        long userId = Long.parseLong(String.valueOf(loginId));
        User user = userMapper.selectById(userId);
        if (user == null || user.getUserRole() == null || user.getUserRole().isBlank()) {
            return List.of();
        }
        return List.of(user.getUserRole());
    }

    /**
     * 返回当前登录账号的权限列表。
     *
     * <p>本项目暂无全局权限码，返回空列表。</p>
     *
     * @param loginId   登录 ID（即用户 ID）
     * @param loginType 登录类型
     * @return 权限列表（当前为空）
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return List.of();
    }
}
