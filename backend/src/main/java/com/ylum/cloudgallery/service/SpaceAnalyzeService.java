package com.ylum.cloudgallery.service;

import com.ylum.cloudgallery.model.vo.SpaceCategoryAnalyzeVO;
import com.ylum.cloudgallery.model.vo.SpaceRankAnalyzeVO;
import com.ylum.cloudgallery.model.vo.SpaceSizeAnalyzeVO;
import com.ylum.cloudgallery.model.vo.SpaceTagAnalyzeVO;
import com.ylum.cloudgallery.model.vo.SpaceUsageAnalyzeVO;

import java.util.List;

/**
 * 空间分析服务接口，定义空间使用 / 分类 / 标签 / 大小 / 排行统计能力。
 */
public interface SpaceAnalyzeService {

    /**
     * 空间使用分析：统计已用图片数量与总大小，并计算相对全局上限的使用率。
     *
     * @param spaceId 空间 ID，为空表示公共图库
     * @return 空间使用分析结果
     */
    SpaceUsageAnalyzeVO getSpaceUsageAnalyze(Long spaceId);

    /**
     * 分类分析：按图片 category 分组统计数量与总大小。
     *
     * @param spaceId 空间 ID，为空表示公共图库
     * @return 分类分析结果列表（按数量降序）
     */
    List<SpaceCategoryAnalyzeVO> getSpaceCategoryAnalyze(Long spaceId);

    /**
     * 标签分析：解析图片 tags（JSON 数组）统计每个标签的数量与总大小。
     *
     * @param spaceId 空间 ID，为空表示公共图库
     * @return 标签分析结果列表（按数量降序）
     */
    List<SpaceTagAnalyzeVO> getSpaceTagAnalyze(Long spaceId);

    /**
     * 大小分析：按图片大小区间分组统计数量。
     *
     * @param spaceId 空间 ID，为空表示公共图库
     * @return 大小分析结果列表（按区间从小到大）
     */
    List<SpaceSizeAnalyzeVO> getSpaceSizeAnalyze(Long spaceId);

    /**
     * 空间用量排行：按各空间图片总大小降序返回 TopN（需系统管理员权限）。
     *
     * @param topN 排行数量
     * @return 排行结果列表
     */
    List<SpaceRankAnalyzeVO> getSpaceRankAnalyze(int topN);
}
