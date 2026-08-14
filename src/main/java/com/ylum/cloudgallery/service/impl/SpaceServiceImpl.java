package com.ylum.cloudgallery.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.ylum.cloudgallery.common.BusinessException;
import com.ylum.cloudgallery.common.ErrorCode;
import com.ylum.cloudgallery.constant.SpaceConstant;
import com.ylum.cloudgallery.constant.UserConstant;
import com.ylum.cloudgallery.manager.sharding.DynamicShardingManager;
import com.ylum.cloudgallery.mapper.SpaceMapper;
import com.ylum.cloudgallery.mapper.SpaceUserMapper;
import com.ylum.cloudgallery.mapper.UserMapper;
import com.ylum.cloudgallery.model.dto.SpaceAddRequest;
import com.ylum.cloudgallery.model.dto.SpaceEditRequest;
import com.ylum.cloudgallery.model.entity.Space;
import com.ylum.cloudgallery.model.entity.SpaceUser;
import com.ylum.cloudgallery.model.entity.User;
import com.ylum.cloudgallery.model.vo.SpaceVO;
import com.ylum.cloudgallery.service.SpaceService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * 空间服务实现：创建 / 编辑 / 删除 / 详情 / 列表。
 */
@Service
public class SpaceServiceImpl extends ServiceImpl<SpaceMapper, Space> implements SpaceService {

    @Resource
    private SpaceUserMapper spaceUserMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private DynamicShardingManager dynamicShardingManager;

    /**
     * 创建空间：校验空间类型，以当前登录用户为创建者写入。
     *
     * <p>空间创建成功后，动态创建对应的图片分表 {@code picture_{spaceId}} 并刷新分片规则。
     * 私有 / 团队空间均按 {@code space_id} 分表，因此两种类型都需建表。</p>
     */
    @Override
    public long addSpace(SpaceAddRequest request) {
        Integer spaceType = request.getSpaceType();
        if (!Objects.equals(spaceType, SpaceConstant.SPACE_TYPE_PRIVATE)
                && !Objects.equals(spaceType, SpaceConstant.SPACE_TYPE_TEAM)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间类型不合法");
        }

        long userId = StpUtil.getLoginIdAsLong();

        Space space = new Space();
        space.setSpaceName(request.getSpaceName());
        space.setSpaceType(spaceType);
        space.setUserId(userId);

        boolean saved = this.save(space);
        if (!saved) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建空间失败");
        }
        // 空间落库成功后，动态建图片分表并刷新分片规则（业务事务外顺序执行）
        dynamicShardingManager.createTableForSpace(space.getId());
        return space.getId();
    }

    /**
     * 编辑空间：仅创建者或管理员可修改空间名称。
     */
    @Override
    public void editSpace(SpaceEditRequest request) {
        Space space = getSpaceByIdOrThrow(request.getId());
        checkSpaceOwnerOrAdmin(space);

        space.setSpaceName(request.getSpaceName());
        boolean updated = this.updateById(space);
        if (!updated) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "编辑空间失败");
        }
    }

    /**
     * 删除空间：逻辑删除空间，并物理删除其成员关系。
     */
    @Override
    public void deleteSpace(long id) {
        Space space = getSpaceByIdOrThrow(id);
        checkSpaceOwnerOrAdmin(space);

        boolean removed = this.removeById(id);
        if (!removed) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除空间失败");
        }
        // 成员关系为物理删除，随空间删除一并清理
        spaceUserMapper.delete(new LambdaQueryWrapper<SpaceUser>().eq(SpaceUser::getSpaceId, id));
    }

    /**
     * 查询空间详情。
     */
    @Override
    public SpaceVO getSpaceById(long id) {
        Space space = getSpaceByIdOrThrow(id);
        return toSpaceVO(space);
    }

    /**
     * 查询当前用户创建的空间列表。
     */
    @Override
    public List<SpaceVO> listMySpaces() {
        long userId = StpUtil.getLoginIdAsLong();
        List<Space> spaces = this.lambdaQuery()
                .eq(Space::getUserId, userId)
                .orderByDesc(Space::getCreateTime)
                .list();
        return spaces.stream().map(this::toSpaceVO).toList();
    }

    /**
     * 校验操作者为空间创建者或系统管理员。
     */
    private void checkSpaceOwnerOrAdmin(Space space) {
        long userId = StpUtil.getLoginIdAsLong();
        if (Objects.equals(space.getUserId(), userId)) {
            return;
        }
        User user = userMapper.selectById(userId);
        boolean admin = user != null && UserConstant.hasAdminRole(user.getUserRole());
        if (!admin) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "仅创建者或管理员可操作");
        }
    }

    /**
     * 按 ID 查询空间，不存在则抛异常。
     */
    private Space getSpaceByIdOrThrow(long id) {
        Space space = this.getById(id);
        if (space == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "空间不存在");
        }
        return space;
    }

    /**
     * 实体转视图对象。
     */
    private SpaceVO toSpaceVO(Space space) {
        SpaceVO spaceVO = new SpaceVO();
        BeanUtils.copyProperties(space, spaceVO);
        return spaceVO;
    }
}
