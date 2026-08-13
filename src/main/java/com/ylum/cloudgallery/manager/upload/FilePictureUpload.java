package com.ylum.cloudgallery.manager.upload;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.ylum.cloudgallery.common.BusinessException;
import com.ylum.cloudgallery.common.ErrorCode;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

/**
 * 本地文件上传实现。
 *
 * <p>输入源为 {@link MultipartFile}，校验大小与格式后转存为本地临时文件。</p>
 */
@Component
public class FilePictureUpload extends PictureUploadTemplate {

    /**
     * 校验本地文件：非空、大小 ≤ 2MB、格式合法。
     */
    @Override
    protected void validPicture(Object inputSource) {
        MultipartFile file = (MultipartFile) inputSource;
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "上传文件不能为空");
        }
        validSize(file.getSize());
        validSuffix(FileUtil.getSuffix(file.getOriginalFilename()));
    }

    /**
     * 原始文件名为 MultipartFile 自带的文件名。
     */
    @Override
    protected String getOriginalFilename(Object inputSource) {
        MultipartFile file = (MultipartFile) inputSource;
        String originalFilename = file.getOriginalFilename();
        return StrUtil.isBlank(originalFilename) ? "image.jpg" : originalFilename;
    }

    /**
     * 将 MultipartFile 转存到本地临时文件。
     */
    @Override
    protected void processFile(Object inputSource, File file) throws Exception {
        ((MultipartFile) inputSource).transferTo(file);
    }
}
