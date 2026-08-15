package com.ylum.cloudgallery.manager.upload;

import com.ylum.cloudgallery.common.BusinessException;
import com.ylum.cloudgallery.constant.PictureConstant;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 本地文件上传校验逻辑单元测试：空文件 / 超大小 / 非法格式 / 合法文件。
 */
class FilePictureUploadTest {

    private final FilePictureUpload filePictureUpload = new FilePictureUpload();

    /**
     * 空文件应被拒绝。
     */
    @Test
    void validPicture_emptyFile_throws() {
        MockMultipartFile file = new MockMultipartFile("file", "a.jpg", "image/jpeg", new byte[0]);

        assertThrows(BusinessException.class, () -> filePictureUpload.validPicture(file));
    }

    /**
     * 超过 2MB 的文件应被拒绝。
     */
    @Test
    void validPicture_oversize_throws() {
        byte[] bytes = new byte[(int) PictureConstant.MAX_UPLOAD_SIZE + 1];
        MockMultipartFile file = new MockMultipartFile("file", "a.jpg", "image/jpeg", bytes);

        assertThrows(BusinessException.class, () -> filePictureUpload.validPicture(file));
    }

    /**
     * 不支持的格式（gif）应被拒绝。
     */
    @Test
    void validPicture_invalidFormat_throws() {
        MockMultipartFile file = new MockMultipartFile("file", "a.gif", "image/gif", new byte[10]);

        assertThrows(BusinessException.class, () -> filePictureUpload.validPicture(file));
    }

    /**
     * 无后缀文件名应被拒绝。
     */
    @Test
    void validPicture_noSuffix_throws() {
        MockMultipartFile file = new MockMultipartFile("file", "no-suffix", "image/jpeg", new byte[10]);

        assertThrows(BusinessException.class, () -> filePictureUpload.validPicture(file));
    }

    /**
     * 合法的 png 文件应通过校验。
     */
    @Test
    void validPicture_validFile_pass() {
        MockMultipartFile file = new MockMultipartFile("file", "a.png", "image/png", new byte[10]);

        assertDoesNotThrow(() -> filePictureUpload.validPicture(file));
    }

    /**
     * 大小恰好为 2MB 的合法文件应通过校验（边界）。
     */
    @Test
    void validPicture_boundarySize_pass() {
        byte[] bytes = new byte[(int) PictureConstant.MAX_UPLOAD_SIZE];
        MockMultipartFile file = new MockMultipartFile("file", "a.webp", "image/webp", bytes);

        assertDoesNotThrow(() -> filePictureUpload.validPicture(file));
    }
}
