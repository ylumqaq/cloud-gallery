package com.ylum.cloudgallery.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ylum.cloudgallery.common.BusinessException;
import com.ylum.cloudgallery.common.ErrorCode;
import com.ylum.cloudgallery.constant.SpaceConstant;
import com.ylum.cloudgallery.constant.UserConstant;
import com.ylum.cloudgallery.mapper.PictureMapper;
import com.ylum.cloudgallery.mapper.SpaceMapper;
import com.ylum.cloudgallery.mapper.UserMapper;
import com.ylum.cloudgallery.model.entity.Picture;
import com.ylum.cloudgallery.model.entity.Space;
import com.ylum.cloudgallery.model.entity.User;
import com.ylum.cloudgallery.model.vo.SpaceCategoryAnalyzeVO;
import com.ylum.cloudgallery.model.vo.SpaceRankAnalyzeVO;
import com.ylum.cloudgallery.model.vo.SpaceSizeAnalyzeVO;
import com.ylum.cloudgallery.model.vo.SpaceTagAnalyzeVO;
import com.ylum.cloudgallery.model.vo.SpaceUsageAnalyzeVO;
import com.ylum.cloudgallery.service.SpaceAnalyzeService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 空间分析服务实现：使用 / 分类 / 标签 / 大小 / 排行统计。
 *
 * <p>使用 / 分类 / 大小分析通过 MyBatis-Plus {@code selectMaps} 的 groupBy + 聚合函数
 * （{@code COUNT(*)}、{@code SUM(pic_size)}、{@code CASE WHEN}）完成；
 * 标签分析因 {@code tags} 为 JSON 数组字符串，无法在 SQL 层直接分组，改为拉取后 Java 统计；
 * 排行分析为避免跨分片聚合的不确定性，采用「遍历空间 + 单分片聚合」的方式。</p>
 */
@Service
public class SpaceAnalyzeServiceImpl implements SpaceAnalyzeService {

    /** 未分类占位文案 */
    private static final String UNCATEGORIZED = "未分类";

    /** 大小分析区间（按从小到大顺序定义，用于结果排序） */
    private static final List<String> SIZE_RANGE_ORDER = List.of(
            "<100KB", "100KB-500KB", "500KB-1MB", ">1MB", "未知");

    @Resource
    private PictureMapper pictureMapper;

    @Resource
    private SpaceMapper spaceMapper;

    @Resource
    private UserMapper userMapper;

    /**
     * 空间使用分析。
     */
    @Override
    public SpaceUsageAnalyzeVO getSpaceUsageAnalyze(Long spaceId) {
        QueryWrapper<Picture> wrapper = new QueryWrapper<>();
        wrapper.select("COUNT(*) AS totalCount", "IFNULL(SUM(pic_size), 0) AS totalSize");
        fillSpaceCondition(wrapper, spaceId);

        Map<String, Object> map = pictureMapper.selectMaps(wrapper).get(0);
        long usedCount = toLong(map.get("totalCount"));
        long usedSize = toLong(map.get("totalSize"));

        long maxCount = SpaceConstant.DEFAULT_MAX_PICTURE_COUNT;
        long maxSize = SpaceConstant.DEFAULT_MAX_PICTURE_SIZE;

        SpaceUsageAnalyzeVO vo = new SpaceUsageAnalyzeVO();
        vo.setUsedCount(usedCount);
        vo.setUsedSize(usedSize);
        vo.setMaxCount(maxCount);
        vo.setMaxSize(maxSize);
        vo.setCountUsageRatio(ratio(usedCount, maxCount));
        vo.setSizeUsageRatio(ratio(usedSize, maxSize));
        return vo;
    }

    /**
     * 分类分析：按 category 分组统计。
     */
    @Override
    public List<SpaceCategoryAnalyzeVO> getSpaceCategoryAnalyze(Long spaceId) {
        QueryWrapper<Picture> wrapper = new QueryWrapper<>();
        wrapper.select("IFNULL(category, '" + UNCATEGORIZED + "') AS category",
                        "COUNT(*) AS count",
                        "IFNULL(SUM(pic_size), 0) AS totalSize");
        fillSpaceCondition(wrapper, spaceId);
        wrapper.groupBy("category");

        List<Map<String, Object>> maps = pictureMapper.selectMaps(wrapper);
        List<SpaceCategoryAnalyzeVO> result = new ArrayList<>();
        for (Map<String, Object> map : maps) {
            SpaceCategoryAnalyzeVO vo = new SpaceCategoryAnalyzeVO();
            vo.setCategory(String.valueOf(map.get("category")));
            vo.setCount(toLong(map.get("count")));
            vo.setTotalSize(toLong(map.get("totalSize")));
            result.add(vo);
        }
        // 按图片数量降序
        result.sort(Comparator.comparing(SpaceCategoryAnalyzeVO::getCount).reversed());
        return result;
    }

    /**
     * 标签分析：解析 tags JSON 数组，统计每个标签的数量与总大小。
     */
    @Override
    public List<SpaceTagAnalyzeVO> getSpaceTagAnalyze(Long spaceId) {
        QueryWrapper<Picture> wrapper = new QueryWrapper<>();
        wrapper.select("tags", "pic_size")
                .isNotNull("tags")
                .ne("tags", "");
        fillSpaceCondition(wrapper, spaceId);

        List<Map<String, Object>> maps = pictureMapper.selectMaps(wrapper);

        // tag -> [0]=数量, [1]=总大小
        Map<String, long[]> tagStats = new HashMap<>();
        for (Map<String, Object> map : maps) {
            String tagsJson = (String) map.get("tags");
            long picSize = toLong(map.get("pic_size"));
            // tags 以 JSON 数组字符串存储，解析失败或非数组时跳过
            for (String tag : parseTags(tagsJson)) {
                long[] stats = tagStats.computeIfAbsent(tag, k -> new long[2]);
                stats[0] += 1;
                stats[1] += picSize;
            }
        }

        List<SpaceTagAnalyzeVO> result = new ArrayList<>();
        tagStats.forEach((tag, stats) -> {
            SpaceTagAnalyzeVO vo = new SpaceTagAnalyzeVO();
            vo.setTag(tag);
            vo.setCount(stats[0]);
            vo.setTotalSize(stats[1]);
            result.add(vo);
        });
        // 按图片数量降序
        result.sort(Comparator.comparing(SpaceTagAnalyzeVO::getCount).reversed());
        return result;
    }

    /**
     * 大小分析：按大小区间分组统计。
     */
    @Override
    public List<SpaceSizeAnalyzeVO> getSpaceSizeAnalyze(Long spaceId) {
        QueryWrapper<Picture> wrapper = new QueryWrapper<>();
        wrapper.select("CASE WHEN pic_size IS NULL THEN '未知'"
                        + " WHEN pic_size < 102400 THEN '<100KB'"
                        + " WHEN pic_size < 524288 THEN '100KB-500KB'"
                        + " WHEN pic_size < 1048576 THEN '500KB-1MB'"
                        + " ELSE '>1MB' END AS sizeRange",
                "COUNT(*) AS count");
        fillSpaceCondition(wrapper, spaceId);
        wrapper.groupBy("sizeRange");

        List<Map<String, Object>> maps = pictureMapper.selectMaps(wrapper);
        List<SpaceSizeAnalyzeVO> result = new ArrayList<>();
        for (Map<String, Object> map : maps) {
            SpaceSizeAnalyzeVO vo = new SpaceSizeAnalyzeVO();
            vo.setSizeRange(String.valueOf(map.get("sizeRange")));
            vo.setCount(toLong(map.get("count")));
            result.add(vo);
        }
        // 按预定义区间顺序排序，保证结果稳定
        result.sort(Comparator.comparingInt(vo -> {
            int idx = SIZE_RANGE_ORDER.indexOf(vo.getSizeRange());
            return idx < 0 ? Integer.MAX_VALUE : idx;
        }));
        return result;
    }

    /**
     * 空间用量排行：遍历所有空间，逐个统计图片用量后按总大小降序取 TopN。
     */
    @Override
    public List<SpaceRankAnalyzeVO> getSpaceRankAnalyze(int topN) {
        checkAdmin();

        List<Space> spaces = spaceMapper.selectList(null);
        List<SpaceRankAnalyzeVO> result = new ArrayList<>();
        for (Space space : spaces) {
            QueryWrapper<Picture> wrapper = new QueryWrapper<>();
            wrapper.select("COUNT(*) AS count", "IFNULL(SUM(pic_size), 0) AS totalSize")
                    .eq("space_id", space.getId());
            Map<String, Object> map = pictureMapper.selectMaps(wrapper).get(0);

            SpaceRankAnalyzeVO vo = new SpaceRankAnalyzeVO();
            vo.setSpaceId(space.getId());
            vo.setCount(toLong(map.get("count")));
            vo.setTotalSize(toLong(map.get("totalSize")));
            result.add(vo);
        }

        // 按图片总大小降序，取前 topN
        result.sort(Comparator.comparing(SpaceRankAnalyzeVO::getTotalSize).reversed());
        return result.stream().limit(Math.max(topN, 0)).toList();
    }

    /**
     * 校验当前登录用户是否为系统管理员。
     */
    private void checkAdmin() {
        long userId = StpUtil.getLoginIdAsLong();
        User user = userMapper.selectById(userId);
        boolean admin = user != null && UserConstant.ADMIN_ROLE.equals(user.getUserRole());
        if (!admin) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "仅管理员可查看空间用量排行");
        }
    }

    /**
     * 填充空间过滤条件：spaceId 为空查公共图库（space_id IS NULL），否则查指定空间。
     */
    private void fillSpaceCondition(QueryWrapper<Picture> wrapper, Long spaceId) {
        if (spaceId == null) {
            wrapper.isNull("space_id");
        } else {
            wrapper.eq("space_id", spaceId);
        }
    }

    /**
     * 解析 tags JSON 数组字符串，返回标签列表；解析失败返回空列表。
     */
    private List<String> parseTags(String tagsJson) {
        try {
            return JSONUtil.toList(tagsJson, String.class);
        } catch (Exception e) {
            return List.of();
        }
    }

    /**
     * 将聚合查询返回的数值安全转换为 long。
     */
    private long toLong(Object value) {
        if (value == null) {
            return 0L;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(value.toString());
    }

    /**
     * 计算使用率（百分比，0-100），分母为 0 时返回 0。
     */
    private double ratio(long used, long max) {
        if (max <= 0) {
            return 0.0;
        }
        return used * 100.0 / max;
    }
}
