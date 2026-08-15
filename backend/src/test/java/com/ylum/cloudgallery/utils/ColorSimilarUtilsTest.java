package com.ylum.cloudgallery.utils;

import com.ylum.cloudgallery.common.BusinessException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 颜色相似度工具单元测试：hex 转 RGB 与欧氏距离计算。
 */
class ColorSimilarUtilsTest {

    /**
     * 带 0x 前缀的颜色能正确解析为 RGB。
     */
    @Test
    void hexToRgb_0xPrefix_success() {
        assertArrayEquals(new int[]{224, 0, 0}, ColorSimilarUtils.hexToRgb("0xe00000"));
    }

    /**
     * 带 # 前缀的颜色能正确解析为 RGB。
     */
    @Test
    void hexToRgb_hashPrefix_success() {
        assertArrayEquals(new int[]{255, 0, 0}, ColorSimilarUtils.hexToRgb("#ff0000"));
    }

    /**
     * 无前缀的颜色能正确解析为 RGB。
     */
    @Test
    void hexToRgb_noPrefix_success() {
        assertArrayEquals(new int[]{0, 0, 255}, ColorSimilarUtils.hexToRgb("0000ff"));
    }

    /**
     * 非法颜色值抛出参数异常。
     */
    @Test
    void hexToRgb_invalid_throws() {
        assertThrows(BusinessException.class, () -> ColorSimilarUtils.hexToRgb("xyz"));
        assertThrows(BusinessException.class, () -> ColorSimilarUtils.hexToRgb(""));
        assertThrows(BusinessException.class, () -> ColorSimilarUtils.hexToRgb(null));
    }

    /**
     * 相同颜色距离为 0。
     */
    @Test
    void calculateDistance_sameColor_zero() {
        double distance = ColorSimilarUtils.calculateDistance(
                new int[]{255, 0, 0}, new int[]{255, 0, 0});
        assertEquals(0.0, distance);
    }

    /**
     * 红蓝之间距离为 sqrt(255^2 + 255^2)。
     */
    @Test
    void calculateDistance_redVsBlue() {
        double distance = ColorSimilarUtils.calculateDistance(
                new int[]{255, 0, 0}, new int[]{0, 0, 255});
        assertEquals(Math.sqrt(255.0 * 255.0 + 255.0 * 255.0), distance, 0.0001);
    }
}
