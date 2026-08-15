package com.ylum.cloudgallery.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.ylum.cloudgallery.auth.SpaceUserAuthManager;
import com.ylum.cloudgallery.common.BusinessException;
import com.ylum.cloudgallery.common.ErrorCode;
import com.ylum.cloudgallery.constant.PictureConstant;
import com.ylum.cloudgallery.constant.SpaceUserConstant;
import com.ylum.cloudgallery.manager.CosManager;
import com.ylum.cloudgallery.manager.upload.FilePictureUpload;
import com.ylum.cloudgallery.manager.upload.PictureUploadTemplate;
import com.ylum.cloudgallery.manager.upload.UrlPictureUpload;
import com.ylum.cloudgallery.manager.upload.model.UploadPictureResult;
import com.ylum.cloudgallery.mapper.PictureMapper;
import com.ylum.cloudgallery.mapper.SpaceMapper;
import com.ylum.cloudgallery.model.dto.PictureEditRequest;
import com.ylum.cloudgallery.model.dto.PictureQueryRequest;
import com.ylum.cloudgallery.model.dto.PictureSearchByPictureRequest;
import com.ylum.cloudgallery.model.dto.PictureUploadByBatchRequest;
import com.ylum.cloudgallery.model.dto.PictureUploadRequest;
import com.ylum.cloudgallery.model.entity.Picture;
import com.ylum.cloudgallery.model.entity.Space;
import com.ylum.cloudgallery.model.vo.PictureVO;
import com.ylum.cloudgallery.service.PictureService;
import com.ylum.cloudgallery.service.search.CiImageSearchStrategy;
import com.ylum.cloudgallery.utils.ColorSimilarUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 图片服务实现：上传 / 分页查询 / 详情 / 编辑 / 删除 / 批量抓取。
 *
 * <p>上传采用「先 COS 后 MySQL」的顺序，MySQL 写入失败时补偿删除 COS 对象；
 * 删除采用「先 MySQL 逻辑删除，再删 COS 对象」的顺序，COS 删除失败仅产生孤儿对象，不影响业务。</p>
 */
@Slf4j
@Service
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture> implements PictureService {

    @Resource
    private FilePictureUpload filePictureUpload;

    @Resource
    private UrlPictureUpload urlPictureUpload;

    @Resource
    private CosManager cosManager;

    @Resource
    private SpaceMapper spaceMapper;

    @Resource
    private SpaceUserAuthManager spaceUserAuthManager;

    @Resource
    private CiImageSearchStrategy ciImageSearchStrategy;

    /**
     * 上传图片（本地文件或 URL）。
     */
    @Override
    public PictureVO uploadPicture(PictureUploadRequest request) {
        long userId = StpUtil.getLoginIdAsLong();
        return uploadPictureInternal(request, userId);
    }

    /**
     * 分页查询图片。
     */
    @Override
    public Page<PictureVO> listPictureByPage(PictureQueryRequest request) {
        long userId = StpUtil.getLoginIdAsLong();
        Long spaceId = request.getSpaceId();

        // 空间图片需校验查看权限；公共图库（spaceId 为空）所有登录用户可见
        if (spaceId != null) {
            checkSpacePermission(spaceId, userId, SpaceUserConstant.PERMISSION_PICTURE_VIEW);
        }

        int current = request.getCurrent() > 0 ? request.getCurrent() : 1;
        int pageSize = request.getPageSize() > 0 ? request.getPageSize() : 10;

        LambdaQueryWrapper<Picture> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(spaceId != null, Picture::getSpaceId, spaceId)
                .isNull(spaceId == null, Picture::getSpaceId)
                .like(StrUtil.isNotBlank(request.getSearchText()), Picture::getName, request.getSearchText())
                .eq(StrUtil.isNotBlank(request.getPicColor()), Picture::getPicColor, request.getPicColor())
                .orderByDesc(Picture::getCreateTime);

        Page<Picture> picturePage = this.page(new Page<>(current, pageSize), wrapper);

        Page<PictureVO> voPage = new Page<>(picturePage.getCurrent(), picturePage.getSize(), picturePage.getTotal());
        voPage.setRecords(picturePage.getRecords().stream().map(this::toPictureVO).toList());
        return voPage;
    }

    /**
     * 查询图片详情。
     */
    @Override
    public PictureVO getPictureVOById(long id) {
        long userId = StpUtil.getLoginIdAsLong();
        Picture picture = getPictureByIdOrThrow(id);

        // 空间图片需校验查看权限
        if (picture.getSpaceId() != null) {
            checkSpacePermission(picture.getSpaceId(), userId, SpaceUserConstant.PERMISSION_PICTURE_VIEW);
        }
        return toPictureVO(picture);
    }

    /**
     * 编辑图片（改名 + 移动空间）。
     */
    @Override
    public void editPicture(PictureEditRequest request) {
        long userId = StpUtil.getLoginIdAsLong();
        Picture picture = getPictureByIdOrThrow(request.getId());

        // 校验权限：公共图库图片仅上传者可编辑，空间图片按空间权限
        checkPicturePermission(picture, userId, SpaceUserConstant.PERMISSION_PICTURE_EDIT);

        // 目标空间存在性校验（spaceId 为空表示不移动空间，保持原空间）
        if (request.getSpaceId() != null) {
            Space space = spaceMapper.selectById(request.getSpaceId());
            if (space == null) {
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "目标空间不存在");
            }
            picture.setSpaceId(request.getSpaceId());
        }

        picture.setName(request.getName());
        picture.setCategory(request.getCategory());
        picture.setTags(request.getTags());
        picture.setEditTime(LocalDateTime.now());

        boolean updated = this.updateById(picture);
        if (!updated) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "编辑图片失败");
        }
    }

    /**
     * 删除图片（逻辑删除 MySQL + 删除 COS 对象）。
     */
    @Override
    public void deletePicture(long id) {
        long userId = StpUtil.getLoginIdAsLong();
        Picture picture = getPictureByIdOrThrow(id);

        // 校验权限：公共图库图片仅上传者可删除，空间图片按空间权限
        checkPicturePermission(picture, userId, SpaceUserConstant.PERMISSION_PICTURE_DELETE);

        // 先逻辑删除 MySQL 元数据
        boolean removed = this.removeById(id);
        if (!removed) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除图片失败");
        }

        // CI 图库出库，最终一致，失败不抛异常
        ciImageSearchStrategy.onDelete(picture);

        // 再删除 COS 对象（失败仅产生孤儿对象，不抛异常）
        deleteCosObjects(picture);
    }

    /**
     * 以图搜图：提取查询图特征，检索相似图片并回查 MySQL 详情。
     */
    @Override
    public List<PictureVO> searchPictureByPicture(PictureSearchByPictureRequest request) {
        long userId = StpUtil.getLoginIdAsLong();
        Long spaceId = request.getSpaceId();
        int topK = request.getTopK() != null && request.getTopK() > 0
                ? request.getTopK()
                : PictureConstant.DEFAULT_SEARCH_TOP_K;

        // 空间图片需校验查看权限；公共图库（spaceId 为空）所有登录用户可搜
        if (spaceId != null) {
            checkSpacePermission(spaceId, userId, SpaceUserConstant.PERMISSION_PICTURE_VIEW);
        }

        File queryFile = null;
        try {
            // 将查询图（本地文件或 URL）转为本地临时文件
            queryFile = resolveQueryFile(request);

            // 调用 CI 检索相似图片 ID（空间过滤由下方 MySQL 兜底）
            List<Long> pictureIds = ciImageSearchStrategy.search(queryFile, spaceId, topK);
            if (pictureIds.isEmpty()) {
                return List.of();
            }
            // 回查 MySQL 详情，保留相似度顺序，并兜底过滤空间与已删除图片
            return listPictureVOByIds(pictureIds, spaceId);
        } finally {
            deleteTempFile(queryFile);
        }
    }

    /**
     * 按颜色搜索：计算各图片主色调与目标颜色的 RGB 欧氏距离，按距离升序返回相近图片。
     */
    @Override
    public List<PictureVO> searchPictureByColor(String picColor, Long spaceId, int topN) {
        long userId = StpUtil.getLoginIdAsLong();

        // 空间图片需校验查看权限；公共图库（spaceId 为空）所有登录用户可搜
        if (spaceId != null) {
            checkSpacePermission(spaceId, userId, SpaceUserConstant.PERMISSION_PICTURE_VIEW);
        }

        // 解析目标颜色，非法时直接抛参数异常
        int[] targetRgb = ColorSimilarUtils.hexToRgb(picColor);

        int limit = topN > 0 ? topN : PictureConstant.DEFAULT_SEARCH_TOP_K;

        // 查询空间内所有具备主色调的图片
        LambdaQueryWrapper<Picture> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(spaceId != null, Picture::getSpaceId, spaceId)
                .isNull(spaceId == null, Picture::getSpaceId)
                .isNotNull(Picture::getPicColor)
                .ne(Picture::getPicColor, "");

        List<Picture> pictures = this.list(wrapper);

        // 按与目标颜色的欧氏距离升序排序，取前 topN
        return pictures.stream()
                .sorted(Comparator.comparingDouble(p -> colorDistance(p, targetRgb)))
                .limit(limit)
                .map(this::toPictureVO)
                .toList();
    }

    /**
     * 批量抓取上传：用 Jsoup 爬取必应图片搜索结果，逐张走 URL 上传流程。
     */
    @Override
    public List<PictureVO> uploadPictureByBatch(PictureUploadByBatchRequest request) {
        long userId = StpUtil.getLoginIdAsLong();

        // 目标空间存在性校验
        if (request.getSpaceId() != null && spaceMapper.selectById(request.getSpaceId()) == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "目标空间不存在");
        }

        int count = request.getCount() != null && request.getCount() > 0
                ? request.getCount()
                : PictureConstant.DEFAULT_BATCH_COUNT;

        List<String> imageUrls = fetchBingImageUrls(request.getSearchText(), count);

        List<PictureVO> results = new ArrayList<>();
        for (String imageUrl : imageUrls) {
            try {
                PictureUploadRequest uploadRequest = new PictureUploadRequest();
                uploadRequest.setFileUrl(imageUrl);
                uploadRequest.setSpaceId(request.getSpaceId());
                uploadRequest.setPicName(request.getSearchText());
                results.add(uploadPictureInternal(uploadRequest, userId));
            } catch (Exception e) {
                // 每张图片独立上传，单张失败不影响其他图片
                log.warn("批量抓取单张图片上传失败，url={}", imageUrl, e);
            }
        }
        return results;
    }

    /**
     * 上传图片内部实现（供单张上传与批量抓取复用）。
     */
    private PictureVO uploadPictureInternal(PictureUploadRequest request, long userId) {
        // 本地文件与 URL 二选一
        PictureUploadTemplate template;
        Object inputSource;
        if (request.getFile() != null && !request.getFile().isEmpty()) {
            template = filePictureUpload;
            inputSource = request.getFile();
        } else if (StrUtil.isNotBlank(request.getFileUrl())) {
            template = urlPictureUpload;
            inputSource = request.getFileUrl();
        } else {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请选择上传文件或填写图片地址");
        }

        // 目标空间存在性校验（spaceId 为空 = 公共图库）
        if (request.getSpaceId() != null && spaceMapper.selectById(request.getSpaceId()) == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "目标空间不存在");
        }

        // 上传路径前缀：公共图库 public，空间 space/{spaceId}
        String uploadPathPrefix = request.getSpaceId() == null
                ? "public"
                : "space/" + request.getSpaceId();

        UploadPictureResult result = template.uploadPicture(inputSource, uploadPathPrefix);

        // 组装并落库
        Picture picture = new Picture();
        picture.setName(StrUtil.isNotBlank(request.getPicName()) ? request.getPicName() : result.getPicName());
        picture.setUrl(result.getUrl());
        picture.setThumbnailUrl(result.getThumbnailUrl());
        picture.setPicSize(result.getPicSize());
        picture.setPicWidth(result.getPicWidth());
        picture.setPicHeight(result.getPicHeight());
        picture.setPicFormat(result.getPicFormat());
        picture.setPicColor(result.getPicColor());
        picture.setCategory(request.getCategory());
        picture.setTags(request.getTags());
        picture.setSpaceId(request.getSpaceId());
        picture.setUserId(userId);

        boolean saved = this.save(picture);
        if (!saved) {
            // MySQL 写入失败，补偿删除已上传的 COS 对象
            compensateDeleteCosObjects(result);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "保存图片失败");
        }
        // 元数据落库后 CI 图库入库，最终一致，失败不影响上传
        ciImageSearchStrategy.onUpload(picture, result);
        return toPictureVO(picture);
    }

    /**
     * 校验当前用户在指定空间（或公共图库）下是否拥有指定权限码。
     */
    private void checkSpacePermission(Long spaceId, long userId, String permission) {
        List<String> permissions = spaceUserAuthManager.getPermissionList(spaceId, userId);
        if (!permissions.contains(permission)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无空间权限");
        }
    }

    /**
     * 校验图片操作权限：公共图库图片仅上传者可操作，空间图片按空间权限码校验。
     */
    private void checkPicturePermission(Picture picture, long userId, String permission) {
        if (picture.getSpaceId() == null) {
            // 公共图库：仅图片上传者可编辑 / 删除
            if (!Objects.equals(picture.getUserId(), userId)) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "仅图片上传者可操作");
            }
            return;
        }
        checkSpacePermission(picture.getSpaceId(), userId, permission);
    }

    /**
     * 按 ID 查询图片，不存在则抛异常。
     */
    private Picture getPictureByIdOrThrow(long id) {
        Picture picture = this.getById(id);
        if (picture == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "图片不存在");
        }
        return picture;
    }

    /**
     * 计算图片主色调与目标颜色的 RGB 欧氏距离；单张图片颜色格式异常时按最不相似处理。
     */
    private double colorDistance(Picture picture, int[] targetRgb) {
        try {
            int[] rgb = ColorSimilarUtils.hexToRgb(picture.getPicColor());
            return ColorSimilarUtils.calculateDistance(targetRgb, rgb);
        } catch (Exception e) {
            return Double.MAX_VALUE;
        }
    }

    /**
     * 实体转视图对象。
     */
    private PictureVO toPictureVO(Picture picture) {
        PictureVO vo = new PictureVO();
        BeanUtils.copyProperties(picture, vo);
        return vo;
    }

    /**
     * 删除图片对应的 COS 对象（原图 + 缩略图 + webp 图）。
     */
    private void deleteCosObjects(Picture picture) {
        try {
            deleteByUrl(picture.getUrl());
            deleteByUrl(picture.getThumbnailUrl());
            // webp 图对象键由原图对象键推导而来
            String webpKey = deriveWebpKey(picture.getUrl());
            if (webpKey != null) {
                cosManager.deleteObject(webpKey);
            }
        } catch (Exception e) {
            log.warn("删除 COS 对象失败，pictureId={}", picture.getId(), e);
        }
    }

    /**
     * 上传失败时补偿删除已上传的 COS 对象。
     */
    private void compensateDeleteCosObjects(UploadPictureResult result) {
        try {
            if (result.getOriginalKey() != null) {
                cosManager.deleteObject(result.getOriginalKey());
            }
            if (result.getWebpKey() != null) {
                cosManager.deleteObject(result.getWebpKey());
            }
            if (result.getThumbnailKey() != null) {
                cosManager.deleteObject(result.getThumbnailKey());
            }
        } catch (Exception e) {
            log.warn("补偿删除 COS 对象失败", e);
        }
    }

    /**
     * 按对象 URL 删除 COS 对象。
     */
    private void deleteByUrl(String url) {
        String key = extractKey(url);
        if (key != null) {
            cosManager.deleteObject(key);
        }
    }

    /**
     * 从对象 URL 提取对象键（URL 路径去掉前导斜杠）。
     */
    private String extractKey(String url) {
        if (StrUtil.isBlank(url)) {
            return null;
        }
        try {
            String path = new URL(url).getPath();
            return path.startsWith("/") ? path.substring(1) : path;
        } catch (MalformedURLException e) {
            return null;
        }
    }

    /**
     * 由原图对象键推导 webp 图对象键（{原图名}_webp.webp）。
     */
    private String deriveWebpKey(String originalUrl) {
        String key = extractKey(originalUrl);
        if (key == null) {
            return null;
        }
        int dotIndex = key.lastIndexOf('.');
        String base = dotIndex > 0 ? key.substring(0, dotIndex) : key;
        return base + "_webp.webp";
    }

    /**
     * 用 Jsoup 爬取必应图片搜索结果，返回图片 URL 列表。
     */
    private List<String> fetchBingImageUrls(String searchText, int count) {
        String url = "https://www.bing.com/images/search?q=" + URLEncoder.encode(searchText, StandardCharsets.UTF_8);
        try {
            Document document = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .timeout(10000)
                    .get();

            Elements images = document.select("img.mimg");
            List<String> urls = new ArrayList<>();
            for (Element img : images) {
                if (urls.size() >= count) {
                    break;
                }
                String src = img.attr("src");
                if (StrUtil.isBlank(src)) {
                    src = img.attr("data-src");
                }
                if (StrUtil.isBlank(src)) {
                    continue;
                }
                if (src.startsWith("//")) {
                    src = "https:" + src;
                }
                if (src.startsWith("http://") || src.startsWith("https://")) {
                    urls.add(src);
                }
            }
            return urls;
        } catch (IOException e) {
            log.error("抓取必应图片搜索结果失败，searchText={}", searchText, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "抓取图片失败");
        }
    }

    /**
     * 将查询图（本地文件或 URL）转为本地临时文件。
     */
    private File resolveQueryFile(PictureSearchByPictureRequest request) {
        String suffix;
        File tempFile;
        try {
            if (request.getFile() != null && !request.getFile().isEmpty()) {
                MultipartFile file = request.getFile();
                suffix = FileUtil.getSuffix(file.getOriginalFilename());
                tempFile = File.createTempFile("query_", "." + (StrUtil.isBlank(suffix) ? "jpg" : suffix));
                file.transferTo(tempFile);
                return tempFile;
            }
            if (StrUtil.isNotBlank(request.getFileUrl())) {
                suffix = getUrlSuffix(request.getFileUrl());
                tempFile = File.createTempFile("query_", "." + (StrUtil.isBlank(suffix) ? "jpg" : suffix));
                HttpUtil.downloadFile(request.getFileUrl(), tempFile);
                return tempFile;
            }
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请选择查询图片或填写图片地址");
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "查询图片处理失败");
        }
    }

    /**
     * 从 URL 提取文件后缀。
     */
    private String getUrlSuffix(String url) {
        try {
            String path = new URL(url).getPath();
            String name = path.substring(path.lastIndexOf('/') + 1);
            return FileUtil.getSuffix(name);
        } catch (MalformedURLException e) {
            return null;
        }
    }

    /**
     * 按相似度顺序回查 MySQL 详情，并兜底过滤空间与已删除图片。
     */
    private List<PictureVO> listPictureVOByIds(List<Long> pictureIds, Long spaceId) {
        LambdaQueryWrapper<Picture> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Picture::getId, pictureIds)
                .eq(spaceId != null, Picture::getSpaceId, spaceId)
                .isNull(spaceId == null, Picture::getSpaceId);
        Map<Long, Picture> pictureMap = this.list(wrapper).stream()
                .collect(Collectors.toMap(Picture::getId, Function.identity()));

        List<PictureVO> result = new ArrayList<>();
        for (Long id : pictureIds) {
            Picture picture = pictureMap.get(id);
            if (picture != null) {
                result.add(toPictureVO(picture));
            }
        }
        return result;
    }

    /**
     * 删除临时文件（忽略失败）。
     */
    private void deleteTempFile(File file) {
        if (file != null && file.exists() && !file.delete()) {
            file.deleteOnExit();
        }
    }
}
