package com.ylum.cloudgallery.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.ylum.cloudgallery.model.dto.SpaceUserAddRequest;
import com.ylum.cloudgallery.model.dto.SpaceUserEditRequest;
import com.ylum.cloudgallery.model.dto.SpaceUserRemoveRequest;
import com.ylum.cloudgallery.model.entity.SpaceUser;
import com.ylum.cloudgallery.model.vo.SpaceUserAuthVO;
import com.ylum.cloudgallery.model.vo.SpaceUserVO;

import java.util.List;

/**
 * 空间成员服务接口，定义成员添加 / 修改角色 / 移除 / 列表 / 权限查询能力。
 */
public interface SpaceUserService extends IService<SpaceUser> {

    /**
     * 添加空间成员。
     *
     * @param request 添加成员请求参数
     * @return 新成员关系 ID
     */
    long addSpaceUser(SpaceUserAddRequest request);

    /**
     * 修改空间成员角色。
     *
     * @param request 修改角色请求参数
     */
    void editSpaceUser(SpaceUserEditRequest request);

    /**
     * 移除空间成员。
     *
     * @param request 移除成员请求参数
     */
    void removeSpaceUser(SpaceUserRemoveRequest request);

    /**
     * 查询空间成员列表（附带用户展示信息）。
     *
     * @param spaceId 空间 ID
     * @return 成员视图对象列表
     */
    List<SpaceUserVO> listSpaceUsers(long spaceId);

    /**
     * 查询当前用户在某空间的角色与权限码列表。
     *
     * @param spaceId 空间 ID
     * @return 角色与权限信息
     */
    SpaceUserAuthVO getCurrentUserAuth(long spaceId);
}
