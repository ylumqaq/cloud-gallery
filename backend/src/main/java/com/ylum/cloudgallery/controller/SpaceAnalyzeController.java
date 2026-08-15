package com.ylum.cloudgallery.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.ylum.cloudgallery.auth.SaSpaceCheckPermission;
import com.ylum.cloudgallery.common.BaseResponse;
import com.ylum.cloudgallery.common.ResultUtils;
import com.ylum.cloudgallery.constant.SpaceUserConstant;
import com.ylum.cloudgallery.model.vo.SpaceCategoryAnalyzeVO;
import com.ylum.cloudgallery.model.vo.SpaceRankAnalyzeVO;
import com.ylum.cloudgallery.model.vo.SpaceSizeAnalyzeVO;
import com.ylum.cloudgallery.model.vo.SpaceTagAnalyzeVO;
import com.ylum.cloudgallery.model.vo.SpaceUsageAnalyzeVO;
import com.ylum.cloudgallery.service.SpaceAnalyzeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 空间分析控制器：使用 / 分类 / 标签 / 大小 / 排行统计。
 *
 * <p>接口前缀为 {@code /api/space/analyze}。除排行接口需系统管理员外，
 * 其余接口需登录且拥有空间查看权限（公共图库所有登录用户可查看）。</p>
 */
@Tag(name = "空间分析模块", description = "空间使用 / 分类 / 标签 / 大小 / 排行统计")
@RestController
@RequestMapping("/space/analyze")
public class SpaceAnalyzeController {

    private final SpaceAnalyzeService spaceAnalyzeService;

    public SpaceAnalyzeController(SpaceAnalyzeService spaceAnalyzeService) {
        this.spaceAnalyzeService = spaceAnalyzeService;
    }

    /**
     * 空间使用分析（需登录 + 空间查看权限）。
     */
    @Operation(summary = "空间使用分析")
    @GetMapping("/usage")
    @SaCheckLogin
    @SaSpaceCheckPermission(SpaceUserConstant.PERMISSION_PICTURE_VIEW)
    public BaseResponse<SpaceUsageAnalyzeVO> getSpaceUsageAnalyze(
            @RequestParam(value = "spaceId", required = false) Long spaceId) {
        return ResultUtils.success(spaceAnalyzeService.getSpaceUsageAnalyze(spaceId));
    }

    /**
     * 分类分析（需登录 + 空间查看权限）。
     */
    @Operation(summary = "分类分析")
    @GetMapping("/category")
    @SaCheckLogin
    @SaSpaceCheckPermission(SpaceUserConstant.PERMISSION_PICTURE_VIEW)
    public BaseResponse<List<SpaceCategoryAnalyzeVO>> getSpaceCategoryAnalyze(
            @RequestParam(value = "spaceId", required = false) Long spaceId) {
        return ResultUtils.success(spaceAnalyzeService.getSpaceCategoryAnalyze(spaceId));
    }

    /**
     * 标签分析（需登录 + 空间查看权限）。
     */
    @Operation(summary = "标签分析")
    @GetMapping("/tag")
    @SaCheckLogin
    @SaSpaceCheckPermission(SpaceUserConstant.PERMISSION_PICTURE_VIEW)
    public BaseResponse<List<SpaceTagAnalyzeVO>> getSpaceTagAnalyze(
            @RequestParam(value = "spaceId", required = false) Long spaceId) {
        return ResultUtils.success(spaceAnalyzeService.getSpaceTagAnalyze(spaceId));
    }

    /**
     * 大小分析（需登录 + 空间查看权限）。
     */
    @Operation(summary = "大小分析")
    @GetMapping("/size")
    @SaCheckLogin
    @SaSpaceCheckPermission(SpaceUserConstant.PERMISSION_PICTURE_VIEW)
    public BaseResponse<List<SpaceSizeAnalyzeVO>> getSpaceSizeAnalyze(
            @RequestParam(value = "spaceId", required = false) Long spaceId) {
        return ResultUtils.success(spaceAnalyzeService.getSpaceSizeAnalyze(spaceId));
    }

    /**
     * 空间用量排行（需登录 + 系统管理员权限）。
     */
    @Operation(summary = "空间用量排行")
    @GetMapping("/rank")
    @SaCheckLogin
    public BaseResponse<List<SpaceRankAnalyzeVO>> getSpaceRankAnalyze(
            @RequestParam(value = "topN", defaultValue = "10") int topN) {
        return ResultUtils.success(spaceAnalyzeService.getSpaceRankAnalyze(topN));
    }
}
