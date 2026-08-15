package com.ylum.cloudgallery.manager.upload;

import com.ylum.cloudgallery.common.BusinessException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * URL 上传校验逻辑单元测试：空地址 / 非法协议 / 合法地址。
 */
class UrlPictureUploadTest {

    private final UrlPictureUpload urlPictureUpload = new UrlPictureUpload();

    /**
     * 空地址应被拒绝。
     */
    @Test
    void validPicture_blankUrl_throws() {
        assertThrows(BusinessException.class, () -> urlPictureUpload.validPicture(""));
    }

    /**
     * 非 HTTP/HTTPS 协议应被拒绝。
     */
    @Test
    void validPicture_nonHttp_throws() {
        assertThrows(BusinessException.class, () -> urlPictureUpload.validPicture("ftp://example.com/a.jpg"));
    }

    /**
     * 合法 http 地址应通过校验。
     */
    @Test
    void validPicture_httpUrl_pass() {
        assertDoesNotThrow(() -> urlPictureUpload.validPicture("http://example.com/a.jpg"));
    }

    /**
     * 合法 https 地址应通过校验。
     */
    @Test
    void validPicture_httpsUrl_pass() {
        assertDoesNotThrow(() -> urlPictureUpload.validPicture("https://example.com/a.png"));
    }

    /**
     * 带后缀的文件名应从 URL 中正确提取。
     */
    @Test
    void getOriginalFilename_extractsFilename() {
        assertEquals("a.jpg", urlPictureUpload.getOriginalFilename("https://example.com/path/a.jpg"));
    }
}
