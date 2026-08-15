package com.ylum.cloudgallery.service.search;

import com.ylum.cloudgallery.common.BusinessException;
import com.ylum.cloudgallery.common.ErrorCode;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 本地图片特征向量提取器（无需模型，纯 JDK 实现）。
 *
 * <p>将图片缩放到固定尺寸 {@code 16×16}，取每个像素的 R、G 两个通道并归一化到
 * {@code [0, 1]}，得到固定 512 维特征向量。视觉上颜色、构图相近的图片会得到相近的向量，
 * 可作为语义级模型（如 CLIP）缺失时的降级实现，保证以图搜图链路在本地即可完整跑通。</p>
 */
@Component
public class LocalImageEmbeddingExtractor {

    /** 目标向量维度，需与 picture_embedding.embedding vector(512) 一致 */
    private static final int DIMENSION = 512;

    /** 缩放后的边长（16×16 = 256 像素，每像素取 R、G 两通道共 512 维） */
    private static final int SIZE = 16;

    /**
     * 提取图片特征向量。
     *
     * @param file 本地图片文件
     * @return 512 维特征向量
     */
    public List<Float> extract(File file) {
        BufferedImage image = readImage(file);
        BufferedImage scaled = scale(image);

        List<Float> vector = new ArrayList<>(DIMENSION);
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                int rgb = scaled.getRGB(x, y);
                int red = (rgb >> 16) & 0xFF;
                int green = (rgb >> 8) & 0xFF;
                vector.add(red / 255.0f);
                vector.add(green / 255.0f);
            }
        }
        return vector;
    }

    /**
     * 读取图片，无法解析时抛出参数异常。
     */
    private BufferedImage readImage(File file) {
        try {
            BufferedImage image = ImageIO.read(file);
            if (image == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "图片解析失败");
            }
            return image;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "图片解析失败");
        }
    }

    /**
     * 使用双线性插值将图片缩放到固定尺寸，保证不同分辨率图片得到一致的向量长度。
     */
    private BufferedImage scale(BufferedImage source) {
        BufferedImage scaled = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = scaled.createGraphics();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            graphics.drawImage(source, 0, 0, SIZE, SIZE, null);
        } finally {
            graphics.dispose();
        }
        return scaled;
    }
}
