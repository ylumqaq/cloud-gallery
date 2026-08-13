package com.ylum.cloudgallery.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.ylum.cloudgallery.common.BaseResponse;
import com.ylum.cloudgallery.common.ResultUtils;
import com.ylum.cloudgallery.model.dto.SpaceAddRequest;
import com.ylum.cloudgallery.model.dto.SpaceDeleteRequest;
import com.ylum.cloudgallery.model.dto.SpaceEditRequest;
import com.ylum.cloudgallery.model.vo.SpaceVO;
import com.ylum.cloudgallery.service.SpaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 空间控制器：创建 / 编辑 / 删除 / 详情 / 列表。
 *
 * <p>接口前缀为 {@code /api/space}（上下文路径 /api 由 application.yml 配置）。</p>
 */
@Tag(name = "空间模块", description = "空间创建 / 编辑 / 删除 / 详情 / 列表")
@RestController
@RequestMapping("/space")
public class SpaceController {

    private final SpaceService spaceService;

    public SpaceController(SpaceService spaceService) {
        this.spaceService = spaceService;
    }

    /**
     * 创建空间（需登录）。
     */
    @Operation(summary = "创建空间")
    @PostMapping("/add")
    @SaCheckLogin
    public BaseResponse<Long> addSpace(@Valid @RequestBody SpaceAddRequest request) {
        long spaceId = spaceService.addSpace(request);
        return ResultUtils.success(spaceId);
    }

    /**
     * 编辑空间（需登录，仅创建者或管理员）。
     */
    @Operation(summary = "编辑空间")
    @PostMapping("/edit")
    @SaCheckLogin
    public BaseResponse<Boolean> editSpace(@Valid @RequestBody SpaceEditRequest request) {
        spaceService.editSpace(request);
        return ResultUtils.success(true);
    }

    /**
     * 删除空间（需登录，仅创建者或管理员）。
     */
    @Operation(summary = "删除空间")
    @PostMapping("/delete")
    @SaCheckLogin
    public BaseResponse<Boolean> deleteSpace(@Valid @RequestBody SpaceDeleteRequest request) {
        spaceService.deleteSpace(request.getId());
        return ResultUtils.success(true);
    }

    /**
     * 空间详情（需登录）。
     */
    @Operation(summary = "空间详情")
    @GetMapping("/get/{id}")
    @SaCheckLogin
    public BaseResponse<SpaceVO> getSpaceById(@PathVariable("id") long id) {
        return ResultUtils.success(spaceService.getSpaceById(id));
    }

    /**
     * 我的空间列表（需登录）。
     */
    @Operation(summary = "我的空间列表")
    @GetMapping("/list")
    @SaCheckLogin
    public BaseResponse<List<SpaceVO>> listMySpaces() {
        return ResultUtils.success(spaceService.listMySpaces());
    }
}
