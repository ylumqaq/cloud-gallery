package com.ylum.cloudgallery.manager.upload;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.ylum.cloudgallery.common.BusinessException;
import com.ylum.cloudgallery.common.ErrorCode;
import org.springframework.stereotype.Component;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * URL 图片上传实现。
 *
 * <p>输入源为网络图片地址字符串，校验协议后下载到本地临时文件，再统一校验大小与格式。</p>
 */
@Component
public class UrlPictureUpload extends PictureUploadTemplate {

    /**
     * 校验 URL：非空且仅支持 HTTP / HTTPS 协议。
     */
    @Override
    protected void validPicture(Object inputSource) {
        String fileUrl = (String) inputSource;
        if (StrUtil.isBlank(fileUrl)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "图片地址不能为空");
        }
        if (!fileUrl.startsWith("http://") && !fileUrl.startsWith("https://")) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "仅支持 HTTP / HTTPS 协议");
        }
    }

    /**
     * 从 URL 路径提取文件名，解析失败时默认 image.jpg。
     */
    @Override
    protected String getOriginalFilename(Object inputSource) {
        String fileUrl = (String) inputSource;
        try {
            String path = new URL(fileUrl).getPath();
            String filename = path.substring(path.lastIndexOf('/') + 1);
            if (StrUtil.isNotBlank(filename) && filename.contains(".")) {
                return filename;
            }
        } catch (MalformedURLException ignored) {
            // URL 无法解析时回退默认文件名
        }
        return "image.jpg";
    }

    /**
     * 下载网络图片到本地临时文件，并校验下载文件的大小与格式。
     */
    @Override
    protected void processFile(Object inputSource, File file) {
        String fileUrl = (String) inputSource;
        HttpUtil.downloadFile(fileUrl, file);
        // 下载后校验真实文件大小与格式
        validSize(file.length());
        validSuffix(FileUtil.getSuffix(getOriginalFilename(inputSource)));
    }
}
