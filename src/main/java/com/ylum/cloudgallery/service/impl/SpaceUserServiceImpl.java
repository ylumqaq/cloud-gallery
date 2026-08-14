package com.ylum.cloudgallery.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.ylum.cloudgallery.auth.SpaceUserAuthManager;
import com.ylum.cloudgallery.common.BusinessException;
import com.ylum.cloudgallery.common.ErrorCode;
import com.ylum.cloudgallery.constant.SpaceUserConstant;
import com.ylum.cloudgallery.mapper.SpaceMapper;
import com.ylum.cloudgallery.mapper.SpaceUserMapper;
import com.ylum.cloudgallery.mapper.UserMapper;
import com.ylum.cloudgallery.model.dto.SpaceUserAddRequest;
import com.ylum.cloudgallery.model.dto.SpaceUserEditRequest;
import com.ylum.cloudgallery.model.dto.SpaceUserRemoveRequest;
import com.ylum.cloudgallery.model.entity.Space;
import com.ylum.cloudgallery.model.entity.SpaceUser;
import com.ylum.cloudgallery.model.entity.User;
import com.ylum.cloudgallery.model.vo.SpaceUserAuthVO;
import com.ylum.cloudgallery.model.vo.SpaceUserVO;
import com.ylum.cloudgallery.service.SpaceUserService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 空间成员服务实现：添加 / 修改角色 / 移除 / 列表 / 权限查询。
 */
@Service
public class SpaceUserServiceImpl extends ServiceImpl<SpaceUserMapper, SpaceUser> implements SpaceUserService {

    @Resource
    private SpaceMapper spaceMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private SpaceUserAuthManager spaceUserAuthManager;

    /**
     * 添加空间成员。
     */
    @Override
    public long addSpaceUser(SpaceUserAddRequest request) {
        Long spaceId = request.getSpaceId();
        Long userId = request.getUserId();
        String spaceRole = request.getSpaceRole();

        Space space = getSpaceByIdOrThrow(spaceId);
        checkRoleValid(spaceRole);

        // 被添加用户必须存在
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "被添加用户不存在");
        }

        // 创建者已隐含拥有管理员权限，无需重复添加为成员
        if (Objects.equals(space.getUserId(), userId)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "创建者无需添加为成员");
        }

        // 成员关系唯一（space_id + user_id），重复添加直接拦截
        SpaceUser exist = getSpaceUser(spaceId, userId);
        if (exist != null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "该用户已是空间成员");
        }

        SpaceUser spaceUser = new SpaceUser();
        spaceUser.setSpaceId(spaceId);
        spaceUser.setUserId(userId);
        spaceUser.setSpaceRole(spaceRole);

        boolean saved = this.save(spaceUser);
        if (!saved) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "添加成员失败");
        }
        return spaceUser.getId();
    }

    /**
     * 修改空间成员角色。
     */
    @Override
    public void editSpaceUser(SpaceUserEditRequest request) {
        Long spaceId = request.getSpaceId();
        Long userId = request.getUserId();
        String spaceRole = request.getSpaceRole();

        getSpaceByIdOrThrow(spaceId);
        checkRoleValid(spaceRole);

        SpaceUser spaceUser = getSpaceUser(spaceId, userId);
        if (spaceUser == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "该用户不是空间成员");
        }

        spaceUser.setSpaceRole(spaceRole);
        boolean updated = this.updateById(spaceUser);
        if (!updated) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "修改角色失败");
        }
    }

    /**
     * 移除空间成员。
     */
    @Override
    public void removeSpaceUser(SpaceUserRemoveRequest request) {
        Long spaceId = request.getSpaceId();
        Long userId = request.getUserId();

        getSpaceByIdOrThrow(spaceId);

        SpaceUser spaceUser = getSpaceUser(spaceId, userId);
        if (spaceUser == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "该用户不是空间成员");
        }

        boolean removed = this.removeById(spaceUser.getId());
        if (!removed) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "移除成员失败");
        }
    }

    /**
     * 查询空间成员列表（附带用户展示信息）。
     */
    @Override
    public List<SpaceUserVO> listSpaceUsers(long spaceId) {
        List<SpaceUser> spaceUsers = this.lambdaQuery()
                .eq(SpaceUser::getSpaceId, spaceId)
                .orderByAsc(SpaceUser::getCreateTime)
                .list();

        // 批量查询用户，组装展示信息
        List<Long> userIds = spaceUsers.stream()
                .map(SpaceUser::getUserId)
                .distinct()
                .toList();
        Map<Long, User> userMap = userIds.isEmpty()
                ? Collections.emptyMap()
                : userMapper.selectList(new LambdaQueryWrapper<User>().in(User::getId, userIds)).stream()
                        .collect(Collectors.toMap(User::getId, user -> user));

        return spaceUsers.stream().map(spaceUser -> {
            SpaceUserVO vo = new SpaceUserVO();
            BeanUtils.copyProperties(spaceUser, vo);
            User user = userMap.get(spaceUser.getUserId());
            if (user != null) {
                vo.setUserName(user.getUserName());
                vo.setUserAccount(user.getUserAccount());
                vo.setUserAvatar(user.getUserAvatar());
            }
            return vo;
        }).toList();
    }

    /**
     * 查询当前用户在某空间的角色与权限码列表。
     */
    @Override
    public SpaceUserAuthVO getCurrentUserAuth(long spaceId) {
        long userId = StpUtil.getLoginIdAsLong();

        SpaceUserAuthVO vo = new SpaceUserAuthVO();
        vo.setRole(spaceUserAuthManager.getRole(spaceId, userId));
        vo.setPermissions(spaceUserAuthManager.getPermissionList(spaceId, userId));
        return vo;
    }

    /**
     * 按 ID 查询空间，不存在则抛异常。
     */
    private Space getSpaceByIdOrThrow(long spaceId) {
        Space space = spaceMapper.selectById(spaceId);
        if (space == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "空间不存在");
        }
        return space;
    }

    /**
     * 查询空间成员关系（spaceId + userId 唯一）。
     */
    private SpaceUser getSpaceUser(long spaceId, long userId) {
        return this.getOne(new LambdaQueryWrapper<SpaceUser>()
                .eq(SpaceUser::getSpaceId, spaceId)
                .eq(SpaceUser::getUserId, userId));
    }

    /**
     * 校验角色取值合法（viewer / editor / admin）。
     */
    private void checkRoleValid(String spaceRole) {
        if (!SpaceUserConstant.ROLE_VIEWER.equals(spaceRole)
                && !SpaceUserConstant.ROLE_EDITOR.equals(spaceRole)
                && !SpaceUserConstant.ROLE_ADMIN.equals(spaceRole)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "角色不合法");
        }
    }
}
