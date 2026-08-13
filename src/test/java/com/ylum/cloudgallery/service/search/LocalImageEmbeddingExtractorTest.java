package com.ylum.cloudgallery.service.search;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * 本地图片特征向量提取器单元测试：维度固定 / 确定性 / 不同图片可区分。
 */
class LocalImageEmbeddingExtractorTest {

    private final LocalImageEmbeddingExtractor extractor = new LocalImageEmbeddingExtractor();

    @TempDir
    File tempDir;

    /**
     * 生成一张指定颜色的纯色 PNG 测试图片。
     */
    private File createImage(String name, Color color) throws Exception {
        BufferedImage image = new BufferedImage(64, 64, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(color);
        graphics.fillRect(0, 0, 64, 64);
        graphics.dispose();

        File file = new File(tempDir, name);
        ImageIO.write(image, "png", file);
        return file;
    }

    /**
     * 提取结果应为固定 512 维。
     */
    @Test
    void extract_returns512Dimensions() throws Exception {
        List<Float> vector = extractor.extract(createImage("red.png", Color.RED));

        assertEquals(512, vector.size());
    }

    /**
     * 相同内容的图片应得到完全相同的向量（确定性）。
     */
    @Test
    void extract_sameImage_sameVector() throws Exception {
        List<Float> v1 = extractor.extract(createImage("a.png", Color.RED));
        List<Float> v2 = extractor.extract(createImage("b.png", Color.RED));

        assertEquals(v1, v2);
    }

    /**
     * 颜色不同的图片应得到不同的向量。
     */
    @Test
    void extract_differentColor_differentVector() throws Exception {
        List<Float> red = extractor.extract(createImage("red.png", Color.RED));
        List<Float> blue = extractor.extract(createImage("blue.png", Color.BLUE));

        assertNotEquals(red, blue);
    }
}
