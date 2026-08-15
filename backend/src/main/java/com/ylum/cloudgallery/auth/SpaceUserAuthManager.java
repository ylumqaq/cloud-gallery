package com.ylum.cloudgallery.auth;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ylum.cloudgallery.common.BusinessException;
import com.ylum.cloudgallery.common.ErrorCode;
import com.ylum.cloudgallery.constant.SpaceConstant;
import com.ylum.cloudgallery.constant.SpaceUserConstant;
import com.ylum.cloudgallery.mapper.SpaceMapper;
import com.ylum.cloudgallery.mapper.SpaceUserMapper;
import com.ylum.cloudgallery.model.entity.Space;
import com.ylum.cloudgallery.model.entity.SpaceUser;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 空间成员与权限管理器。
 *
 * <p>负责「配置化」加载空间权限配置，并在公共图库 / 私有空间 / 团队空间
 * 三种场景下收敛出当前用户的权限码列表，是空间权限鉴权的核心。</p>
 */
@Component
public class SpaceUserAuthManager {

    @Resource
    private SpaceMapper spaceMapper;

    @Resource
    private SpaceUserMapper spaceUserMapper;

    /** 空间权限配置（启动时从 spaceUserAuthConfig.json 加载） */
    private SpaceUserAuthConfig authConfig;

    /**
     * 启动时加载空间权限配置。
     */
    @PostConstruct
    public void init() throws IOException {
        ClassPathResource resource = new ClassPathResource("spaceUserAuthConfig.json");
        try (InputStream in = resource.getInputStream()) {
            authConfig = new ObjectMapper().readValue(in, SpaceUserAuthConfig.class);
        }
    }

    /**
     * 获取指定用户在指定空间（或公共图库）下的权限码列表。
     *
     * <p>三种场景分支收敛：
     * <ul>
     *   <li>公共图库（spaceId 为空）：返回公共权限；</li>
     *   <li>私有空间：仅创建者拥有全部权限，其余用户无权限；</li>
     *   <li>团队空间：按成员角色映射权限码。</li>
     * </ul></p>
     *
     * @param spaceId 空间 ID，为空表示公共图库
     * @param userId  用户 ID
     * @return 权限码列表
     */
    public List<String> getPermissionList(Long spaceId, Long userId) {
        // 公共图库：所有登录用户拥有公共权限
        if (spaceId == null) {
            return authConfig.getPublicPermissions();
        }

        Space space = spaceMapper.selectById(spaceId);
        if (space == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "空间不存在");
        }

        // 创建者拥有管理员全部权限
        if (Objects.equals(space.getUserId(), userId)) {
            return getAdminPermissions();
        }

        // 私有空间：非创建者无权限
        if (Objects.equals(space.getSpaceType(), SpaceConstant.SPACE_TYPE_PRIVATE)) {
            return Collections.emptyList();
        }

        // 团队空间：按成员角色映射权限码
        SpaceUser spaceUser = getSpaceUser(spaceId, userId);
        if (spaceUser == null) {
            return Collections.emptyList();
        }
        return authConfig.getRolePermissionMap().getOrDefault(spaceUser.getSpaceRole(), Collections.emptyList());
    }

    /**
     * 获取指定用户在指定空间下的角色。
     *
     * @param spaceId 空间 ID，为空表示公共图库
     * @param userId  用户 ID
     * @return 角色：creator / viewer / editor / admin；公共图库或非成员返回 null
     */
    public String getRole(Long spaceId, Long userId) {
        if (spaceId == null) {
            return null;
        }
        Space space = spaceMapper.selectById(spaceId);
        if (space == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "空间不存在");
        }
        // 创建者
        if (Objects.equals(space.getUserId(), userId)) {
            return "creator";
        }
        // 私有空间：非创建者无角色
        if (Objects.equals(space.getSpaceType(), SpaceConstant.SPACE_TYPE_PRIVATE)) {
            return null;
        }
        SpaceUser spaceUser = getSpaceUser(spaceId, userId);
        return spaceUser == null ? null : spaceUser.getSpaceRole();
    }

    /**
     * 查询空间成员关系（spaceId + userId 唯一）。
     */
    private SpaceUser getSpaceUser(Long spaceId, Long userId) {
        return spaceUserMapper.selectOne(new LambdaQueryWrapper<SpaceUser>()
                .eq(SpaceUser::getSpaceId, spaceId)
                .eq(SpaceUser::getUserId, userId));
    }

    /**
     * 返回管理员（admin）角色对应的权限码列表，创建者拥有同等权限。
     */
    private List<String> getAdminPermissions() {
        return authConfig.getRolePermissionMap().getOrDefault(SpaceUserConstant.ROLE_ADMIN, Collections.emptyList());
    }
}
