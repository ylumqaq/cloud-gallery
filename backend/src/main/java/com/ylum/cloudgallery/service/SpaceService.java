package com.ylum.cloudgallery.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.ylum.cloudgallery.model.dto.SpaceAddRequest;
import com.ylum.cloudgallery.model.dto.SpaceEditRequest;
import com.ylum.cloudgallery.model.entity.Space;
import com.ylum.cloudgallery.model.vo.SpaceVO;

import java.util.List;

/**
 * 空间服务接口，定义空间的创建 / 编辑 / 删除 / 详情 / 列表能力。
 */
public interface SpaceService extends IService<Space> {

    /**
     * 创建空间。
     *
     * @param request 创建空间请求参数
     * @return 新空间 ID
     */
    long addSpace(SpaceAddRequest request);

    /**
     * 编辑空间（仅创建者或管理员）。
     *
     * @param request 编辑空间请求参数
     */
    void editSpace(SpaceEditRequest request);

    /**
     * 删除空间（仅创建者或管理员），同时清理空间成员关系。
     *
     * @param id 空间 ID
     */
    void deleteSpace(long id);

    /**
     * 查询空间详情。
     *
     * @param id 空间 ID
     * @return 空间视图对象
     */
    SpaceVO getSpaceById(long id);

    /**
     * 查询当前用户创建的空间列表。
     *
     * @return 空间视图对象列表
     */
    List<SpaceVO> listMySpaces();
}
