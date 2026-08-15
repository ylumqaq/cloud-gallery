package com.ylum.cloudgallery.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ylum.cloudgallery.auth.SaSpaceCheckPermission;
import com.ylum.cloudgallery.common.BaseResponse;
import com.ylum.cloudgallery.common.ResultUtils;
import com.ylum.cloudgallery.constant.SpaceUserConstant;
import com.ylum.cloudgallery.model.dto.PictureDeleteRequest;
import com.ylum.cloudgallery.model.dto.PictureEditRequest;
import com.ylum.cloudgallery.model.dto.PictureQueryRequest;
import com.ylum.cloudgallery.model.dto.PictureSearchByPictureRequest;
import com.ylum.cloudgallery.model.dto.PictureUploadByBatchRequest;
import com.ylum.cloudgallery.model.dto.PictureUploadRequest;
import com.ylum.cloudgallery.model.vo.PictureVO;
import com.ylum.cloudgallery.service.PictureService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 图片控制器：上传 / 分页查询 / 详情 / 编辑 / 删除 / 批量抓取。
 *
 * <p>接口前缀为 {@code /api/picture}（上下文路径 /api 由 application.yml 配置）。</p>
 */
@Tag(name = "图片模块", description = "图片上传 / 查询 / 编辑 / 删除 / 批量抓取")
@RestController
@RequestMapping("/picture")
public class PictureController {

    private final PictureService pictureService;

    public PictureController(PictureService pictureService) {
        this.pictureService = pictureService;
    }

    /**
     * 上传图片（本地文件或 URL，需登录 + 空间上传权限）。
     */
    @Operation(summary = "上传图片")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @SaCheckLogin
    @SaSpaceCheckPermission(SpaceUserConstant.PERMISSION_PICTURE_UPLOAD)
    public BaseResponse<PictureVO> uploadPicture(@ModelAttribute PictureUploadRequest request) {
        return ResultUtils.success(pictureService.uploadPicture(request));
    }

    /**
     * 分页查询图片（需登录，公共图库可看，空间图片按权限）。
     */
    @Operation(summary = "分页查询图片")
    @GetMapping("/list/page/vo")
    @SaCheckLogin
    public BaseResponse<Page<PictureVO>> listPictureByPage(@ParameterObject PictureQueryRequest request) {
        return ResultUtils.success(pictureService.listPictureByPage(request));
    }

    /**
     * 图片详情（需登录）。
     */
    @Operation(summary = "图片详情")
    @GetMapping("/get/{id}")
    @SaCheckLogin
    public BaseResponse<PictureVO> getPictureById(@PathVariable("id") long id) {
        return ResultUtils.success(pictureService.getPictureVOById(id));
    }

    /**
     * 编辑图片（需登录）。
     */
    @Operation(summary = "编辑图片")
    @PostMapping("/edit")
    @SaCheckLogin
    public BaseResponse<Boolean> editPicture(@Valid @RequestBody PictureEditRequest request) {
        pictureService.editPicture(request);
        return ResultUtils.success(true);
    }

    /**
     * 删除图片（需登录）。
     */
    @Operation(summary = "删除图片")
    @PostMapping("/delete")
    @SaCheckLogin
    public BaseResponse<Boolean> deletePicture(@Valid @RequestBody PictureDeleteRequest request) {
        pictureService.deletePicture(request.getId());
        return ResultUtils.success(true);
    }

    /**
     * 批量抓取上传（需登录）。
     */
    @Operation(summary = "批量抓取上传")
    @PostMapping("/upload/batch")
    @SaCheckLogin
    public BaseResponse<List<PictureVO>> uploadPictureByBatch(@Valid @RequestBody PictureUploadByBatchRequest request) {
        return ResultUtils.success(pictureService.uploadPictureByBatch(request));
    }

    /**
     * 以图搜图（需登录，查询图支持本地文件或 URL）。
     */
    @Operation(summary = "以图搜图")
    @PostMapping(value = "/search/by/picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @SaCheckLogin
    public BaseResponse<List<PictureVO>> searchPictureByPicture(@ModelAttribute PictureSearchByPictureRequest request) {
        return ResultUtils.success(pictureService.searchPictureByPicture(request));
    }

    /**
     * 按颜色搜索（需登录，按主色调 RGB 欧氏距离排序返回相近图片）。
     */
    @Operation(summary = "按颜色搜索")
    @GetMapping("/search/color")
    @SaCheckLogin
    public BaseResponse<List<PictureVO>> searchPictureByColor(
            @RequestParam("picColor") String picColor,
            @RequestParam(value = "spaceId", required = false) Long spaceId,
            @RequestParam(value = "topN", defaultValue = "20") int topN) {
        return ResultUtils.success(pictureService.searchPictureByColor(picColor, spaceId, topN));
    }
}
