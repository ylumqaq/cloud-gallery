package com.ylum.cloudgallery.utils;

import com.ylum.cloudgallery.common.BusinessException;
import com.ylum.cloudgallery.common.ErrorCode;

/**
 * 颜色相似度计算工具。
 *
 * <p>图片上传时由数据万象 CI 提取的平均主色调以十六进制字符串存储（如 {@code 0xe00000}），
 * 按颜色搜索时将其与目标颜色转为 RGB 后计算欧氏距离，距离越小表示颜色越接近。</p>
 */
public final class ColorSimilarUtils {

    private ColorSimilarUtils() {
    }

    /**
     * 将十六进制颜色字符串解析为 RGB 数组。
     *
     * <p>支持 {@code 0xRRGGBB}、{@code #RRGGBB}、{@code RRGGBB} 三种前缀形式。</p>
     *
     * @param hexColor 十六进制颜色字符串
     * @return 长度为 3 的数组 {@code [r, g, b]}
     * @throws BusinessException 颜色格式非法时抛出参数异常
     */
    public static int[] hexToRgb(String hexColor) {
        if (hexColor == null || hexColor.isBlank()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "颜色值不能为空");
        }
        String hex = hexColor.trim();
        if (hex.startsWith("0x") || hex.startsWith("0X")) {
            hex = hex.substring(2);
        } else if (hex.startsWith("#")) {
            hex = hex.substring(1);
        }
        if (hex.length() != 6) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "颜色格式非法，应为 0xRRGGBB / #RRGGBB / RRGGBB");
        }
        try {
            int r = Integer.parseInt(hex.substring(0, 2), 16);
            int g = Integer.parseInt(hex.substring(2, 4), 16);
            int b = Integer.parseInt(hex.substring(4, 6), 16);
            return new int[]{r, g, b};
        } catch (NumberFormatException e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "颜色格式非法，应为 0xRRGGBB / #RRGGBB / RRGGBB");
        }
    }

    /**
     * 计算两个 RGB 颜色之间的欧氏距离。
     *
     * <p>公式：{@code sqrt((r1-r2)^2 + (g1-g2)^2 + (b1-b2)^2)}。</p>
     *
     * @param rgb1 第一个 RGB 数组
     * @param rgb2 第二个 RGB 数组
     * @return 欧氏距离，值越小颜色越相近
     */
    public static double calculateDistance(int[] rgb1, int[] rgb2) {
        int dr = rgb1[0] - rgb2[0];
        int dg = rgb1[1] - rgb2[1];
        int db = rgb1[2] - rgb2[2];
        return Math.sqrt(dr * dr + dg * dg + db * db);
    }
}
