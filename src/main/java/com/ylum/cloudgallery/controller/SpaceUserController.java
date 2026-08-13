package com.ylum.cloudgallery.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.ylum.cloudgallery.auth.SaSpaceCheckPermission;
import com.ylum.cloudgallery.common.BaseResponse;
import com.ylum.cloudgallery.common.ResultUtils;
import com.ylum.cloudgallery.constant.SpaceUserConstant;
import com.ylum.cloudgallery.model.dto.SpaceUserAddRequest;
import com.ylum.cloudgallery.model.dto.SpaceUserEditRequest;
import com.ylum.cloudgallery.model.dto.SpaceUserRemoveRequest;
import com.ylum.cloudgallery.model.vo.SpaceUserAuthVO;
import com.ylum.cloudgallery.model.vo.SpaceUserVO;
import com.ylum.cloudgallery.service.SpaceUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 空间成员控制器：添加 / 修改角色 / 移除 / 列表 / 当前用户权限。
 *
 * <p>接口前缀为 {@code /api/spaceUser}。除「获取当前用户权限」外，
 * 其余接口均需通过空间权限校验（{@link SaSpaceCheckPermission}）。</p>
 */
@Tag(name = "空间成员模块", description = "空间成员添加 / 修改角色 / 移除 / 列表 / 权限查询")
@RestController
@RequestMapping("/spaceUser")
public class SpaceUserController {

    private final SpaceUserService spaceUserService;

    public SpaceUserController(SpaceUserService spaceUserService) {
        this.spaceUserService = spaceUserService;
    }

    /**
     * 添加成员（需空间管理员权限）。
     */
    @Operation(summary = "添加空间成员")
    @PostMapping("/add")
    @SaCheckLogin
    @SaSpaceCheckPermission(SpaceUserConstant.PERMISSION_SPACE_USER_MANAGE)
    public BaseResponse<Long> addSpaceUser(@Valid @RequestBody SpaceUserAddRequest request) {
        long id = spaceUserService.addSpaceUser(request);
        return ResultUtils.success(id);
    }

    /**
     * 修改成员角色（需空间管理员权限）。
     */
    @Operation(summary = "修改空间成员角色")
    @PostMapping("/edit")
    @SaCheckLogin
    @SaSpaceCheckPermission(SpaceUserConstant.PERMISSION_SPACE_USER_MANAGE)
    public BaseResponse<Boolean> editSpaceUser(@Valid @RequestBody SpaceUserEditRequest request) {
        spaceUserService.editSpaceUser(request);
        return ResultUtils.success(true);
    }

    /**
     * 移除成员（需空间管理员权限）。
     */
    @Operation(summary = "移除空间成员")
    @PostMapping("/delete")
    @SaCheckLogin
    @SaSpaceCheckPermission(SpaceUserConstant.PERMISSION_SPACE_USER_MANAGE)
    public BaseResponse<Boolean> removeSpaceUser(@Valid @RequestBody SpaceUserRemoveRequest request) {
        spaceUserService.removeSpaceUser(request);
        return ResultUtils.success(true);
    }

    /**
     * 成员列表（需空间查看权限）。
     */
    @Operation(summary = "空间成员列表")
    @GetMapping("/list")
    @SaCheckLogin
    @SaSpaceCheckPermission(SpaceUserConstant.PERMISSION_PICTURE_VIEW)
    public BaseResponse<List<SpaceUserVO>> listSpaceUsers(@RequestParam("spaceId") long spaceId) {
        return ResultUtils.success(spaceUserService.listSpaceUsers(spaceId));
    }

    /**
     * 获取当前用户在某空间的角色与权限（需登录）。
     */
    @Operation(summary = "获取当前用户在某空间的权限")
    @GetMapping("/get")
    @SaCheckLogin
    public BaseResponse<SpaceUserAuthVO> getCurrentUserAuth(@RequestParam("spaceId") long spaceId) {
        return ResultUtils.success(spaceUserService.getCurrentUserAuth(spaceId));
    }
}
