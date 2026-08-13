package com.ylum.cloudgallery.service.search;

import com.ylum.cloudgallery.manager.CosManager;
import com.ylum.cloudgallery.manager.upload.model.UploadPictureResult;
import com.ylum.cloudgallery.model.entity.Picture;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * CI 托管图片检索策略单元测试：mock {@link CosManager}，验证入库 / 出库 / 检索 / 临时对象清理逻辑，
 * 不依赖真实腾讯云 COS / 数据万象环境。
 */
@ExtendWith(MockitoExtension.class)
class CiImageSearchStrategyTest {

    @Mock
    private CosManager cosManager;

    @InjectMocks
    private CiImageSearchStrategy strategy;

    @TempDir
    File tempDir;

    /**
     * 上传成功后应把图片加入 CI 图库，entityId 为图片 ID。
     */
    @Test
    void onUpload_addsGalleryImage() {
        Picture picture = new Picture();
        picture.setId(123L);
        UploadPictureResult result = new UploadPictureResult();
        result.setOriginalKey("public/20260813/abc.jpg");

        strategy.onUpload(picture, result);

        verify(cosManager).addGalleryImage("public/20260813/abc.jpg", 123L);
    }

    /**
     * 入库失败不应向上抛出异常（最终一致，不影响上传主流程）。
     */
    @Test
    void onUpload_exception_swallowed() {
        Picture picture = new Picture();
        picture.setId(123L);
        UploadPictureResult result = new UploadPictureResult();
        result.setOriginalKey("public/20260813/abc.jpg");
        doThrow(new RuntimeException("ci 服务异常")).when(cosManager).addGalleryImage(anyString(), anyLong());

        assertDoesNotThrow(() -> strategy.onUpload(picture, result));
    }

    /**
     * 删除图片时应从 URL 解析出对象键并调用 CI 图库出库。
     */
    @Test
    void onDelete_deletesGalleryImage() {
        Picture picture = new Picture();
        picture.setId(123L);
        picture.setUrl("https://bucket.cos.ap-guangzhou.myqcloud.com/public/20260813/abc.jpg");

        strategy.onDelete(picture);

        verify(cosManager).deleteGalleryImage("public/20260813/abc.jpg", 123L);
    }

    /**
     * URL 为空时应跳过出库，不调用 deleteGalleryImage。
     */
    @Test
    void onDelete_blankUrl_skipsDelete() {
        Picture picture = new Picture();
        picture.setId(123L);
        picture.setUrl("");

        strategy.onDelete(picture);

        verify(cosManager, never()).deleteGalleryImage(anyString(), anyLong());
    }

    /**
     * 检索时应先把查询图上传为临时对象，检索后清理该临时对象，并返回 CI 解析出的 pictureId 列表。
     */
    @Test
    void search_uploadsSearchesAndCleansTempObject() throws Exception {
        File queryFile = new File(tempDir, "query.png");
        queryFile.createNewFile();
        when(cosManager.searchGalleryImages(anyString(), eq(10))).thenReturn(List.of(1L, 2L));

        List<Long> result = strategy.search(queryFile, null, 10);

        assertEquals(List.of(1L, 2L), result);
        verify(cosManager).putObject(anyString(), eq(queryFile));
        verify(cosManager).searchGalleryImages(anyString(), eq(10));
        verify(cosManager).deleteObject(anyString());
    }
}
