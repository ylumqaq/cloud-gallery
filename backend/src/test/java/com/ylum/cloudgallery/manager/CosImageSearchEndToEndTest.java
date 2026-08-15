package com.ylum.cloudgallery.manager;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.test.context.SpringBootTest;

import jakarta.annotation.Resource;
import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * CI 以图搜图端到端测试：真实调用腾讯云数据万象「图像搜索」能力（属于内容识别）。
 *
 * <p>完整链路：开通图库 → 上传测试图到 COS → 加入图库（entityId = pictureId）→ 检索 →
 * 出库 → 清理 COS 对象。会产生真实费用与 COS/CI 副作用，且图库容量一次性不可改，
 * 故默认不参与 {@code mvn test}，需显式设置环境变量 {@code RUN_CI_E2E=true} 启用：</p>
 *
 * <pre>
 *   $env:RUN_CI_E2E='true'; mvn -Dtest=CosImageSearchEndToEndTest test
 * </pre>
 */
@Slf4j
@SpringBootTest
@EnabledIfEnvironmentVariable(named = "RUN_CI_E2E", matches = "true",
        disabledReason = "端到端测试：真实调用 CI 图像搜索会产生费用与真实副作用，需设置 RUN_CI_E2E=true 启用")
class CosImageSearchEndToEndTest {

    @Resource
    private CosManager cosManager;

    /**
     * 以图搜图完整链路验证：入库后用同一张图检索，应能命中刚入库的 pictureId。
     */
    @Test
    void imageSearch_endToEnd() throws Exception {
        // 1. 开通图库（幂等：首次开通成功，已开通则忽略报错）
        openImageSearchSafely();

        // 2. 生成测试图片并上传 COS
        File image = createTestImage();
        String key = "search/e2e/" + System.nanoTime() + ".png";
        long pictureId = System.currentTimeMillis();
        cosManager.putObject(key, image);

        try {
            // 3. 加入图库，entityId 保存 pictureId
            cosManager.addGalleryImage(key, pictureId);

            // 4. 检索（入库后建立特征索引可能有延迟，重试等待）
            List<Long> result = searchWithRetry(key, pictureId);
            log.info("CI 检索结果：{}", result);
            assertTrue(result.contains(pictureId), "检索结果应包含刚入库的 pictureId，实际结果：" + result);
        } finally {
            // 5. 出库 + 清理 COS 对象 + 清理临时文件（尽力而为）
            try {
                cosManager.deleteGalleryImage(key, pictureId);
            } catch (Exception e) {
                log.warn("CI 图库出库失败", e);
            }
            try {
                cosManager.deleteObject(key);
            } catch (Exception e) {
                log.warn("删除 COS 对象失败", e);
            }
            image.delete();
        }
    }

    /**
     * 带重试的检索：入库后 CI 建立索引可能有延迟，未命中则等待后重试。
     */
    private List<Long> searchWithRetry(String key, long pictureId) throws InterruptedException {
        for (int i = 0; i < 5; i++) {
            List<Long> result = cosManager.searchGalleryImages(key, 10);
            if (result.contains(pictureId)) {
                return result;
            }
            log.info("第 {} 次检索未命中，等待 CI 建立索引...", i + 1);
            Thread.sleep(2000);
        }
        return cosManager.searchGalleryImages(key, 10);
    }

    /**
     * 开通图库（一次性，图库容量设置后不可修改；已开通时再次调用会报错，忽略即可）。
     */
    private void openImageSearchSafely() {
        try {
            cosManager.openImageSearch("10000", "10");
            log.info("已开通 CI 以图搜图图库");
        } catch (Exception e) {
            log.warn("CI 图库可能已开通，忽略 openImageSearch 失败：{}", e.getMessage());
        }
    }

    /**
     * 生成一张有丰富视觉特征的 PNG 测试图片（多色块 + 圆形，便于 CI 提取有效特征向量）。
     */
    private File createTestImage() throws Exception {
        BufferedImage image = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(Color.RED);
        graphics.fillRect(0, 0, 100, 100);
        graphics.setColor(Color.BLUE);
        graphics.fillRect(100, 0, 100, 100);
        graphics.setColor(Color.GREEN);
        graphics.fillRect(0, 100, 100, 100);
        graphics.setColor(Color.YELLOW);
        graphics.fillRect(100, 100, 100, 100);
        graphics.setColor(Color.BLACK);
        graphics.drawOval(40, 40, 120, 120);
        graphics.dispose();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, "png", out);

        File file = File.createTempFile("ci_e2e_", ".png");
        java.nio.file.Files.write(file.toPath(), out.toByteArray());
        return file;
    }
}
