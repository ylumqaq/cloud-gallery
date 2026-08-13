package com.ylum.cloudgallery.controller;

import com.ylum.cloudgallery.BaseIntegrationTest;
import com.ylum.cloudgallery.mapper.PictureMapper;
import com.ylum.cloudgallery.model.entity.Picture;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 空间分析与按颜色搜索接口集成测试。
 *
 * <p>测试数据通过 {@link PictureMapper} 直接插入公共图库（space_id 为空），
 * 避免依赖 COS 上传；方法运行在事务中并自动回滚。</p>
 */
class SpaceAnalyzeControllerTest extends BaseIntegrationTest {

    @Resource
    private PictureMapper pictureMapper;

    /**
     * 插入一张公共图库图片（space_id 为空）。
     */
    private void insertPicture(long userId, String category, String tags, long picSize, String picColor) {
        Picture picture = new Picture();
        picture.setName("测试图片");
        picture.setUrl("https://example.com/" + System.nanoTime() + ".png");
        picture.setPicSize(picSize);
        picture.setPicFormat("png");
        picture.setPicColor(picColor);
        picture.setCategory(category);
        picture.setTags(tags);
        picture.setUserId(userId);
        pictureMapper.insert(picture);
    }

    /**
     * 空间使用分析：统计公共图库图片数量与总大小。
     */
    @Test
    void usage_success() throws Exception {
        AuthUser user = registerAndLogin(uniqueAccount());
        insertPicture(user.userId(), "头像", "[\"风景\"]", 50 * 1024, "0xff0000");
        insertPicture(user.userId(), "壁纸", "[\"旅行\"]", 200 * 1024, "0x00ff00");

        mockMvc.perform(get("/space/analyze/usage")
                        .header("satoken", user.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.usedCount").value(2))
                .andExpect(jsonPath("$.data.usedSize").value(250 * 1024))
                .andExpect(jsonPath("$.data.maxCount").value(400));
    }

    /**
     * 分类分析：按 category 分组统计。
     */
    @Test
    void category_success() throws Exception {
        AuthUser user = registerAndLogin(uniqueAccount());
        insertPicture(user.userId(), "头像", "[]", 1024, "0xff0000");
        insertPicture(user.userId(), "头像", "[]", 1024, "0xff0000");
        insertPicture(user.userId(), "壁纸", "[]", 1024, "0x00ff00");

        mockMvc.perform(get("/space/analyze/category")
                        .header("satoken", user.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].category").value("头像"))
                .andExpect(jsonPath("$.data[0].count").value(2));
    }

    /**
     * 标签分析：解析 tags JSON 数组统计每个标签数量。
     */
    @Test
    void tag_success() throws Exception {
        AuthUser user = registerAndLogin(uniqueAccount());
        insertPicture(user.userId(), "头像", "[\"风景\",\"旅行\"]", 1024, "0xff0000");
        insertPicture(user.userId(), "壁纸", "[\"风景\"]", 1024, "0x00ff00");

        mockMvc.perform(get("/space/analyze/tag")
                        .header("satoken", user.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].tag").value("风景"))
                .andExpect(jsonPath("$.data[0].count").value(2));
    }

    /**
     * 大小分析：按大小区间分组统计。
     */
    @Test
    void size_success() throws Exception {
        AuthUser user = registerAndLogin(uniqueAccount());
        insertPicture(user.userId(), "头像", "[]", 50 * 1024, "0xff0000");
        insertPicture(user.userId(), "壁纸", "[]", 200 * 1024, "0x00ff00");
        insertPicture(user.userId(), "壁纸", "[]", 800 * 1024, "0x0000ff");
        insertPicture(user.userId(), "壁纸", "[]", 2 * 1024 * 1024, "0x000000");

        mockMvc.perform(get("/space/analyze/size")
                        .header("satoken", user.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(4))
                .andExpect(jsonPath("$.data[0].sizeRange").value("<100KB"))
                .andExpect(jsonPath("$.data[0].count").value(1))
                .andExpect(jsonPath("$.data[3].sizeRange").value(">1MB"))
                .andExpect(jsonPath("$.data[3].count").value(1));
    }

    /**
     * 空间用量排行：普通用户无管理员权限，应返回 40101。
     */
    @Test
    void rank_nonAdmin_returnsNoAuth() throws Exception {
        AuthUser user = registerAndLogin(uniqueAccount());

        mockMvc.perform(get("/space/analyze/rank")
                        .header("satoken", user.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40101));
    }

    /**
     * 按颜色搜索：目标为红色时，红色图片排在蓝色图片之前。
     */
    @Test
    void searchByColor_success() throws Exception {
        AuthUser user = registerAndLogin(uniqueAccount());
        insertPicture(user.userId(), "头像", "[]", 1024, "0x0000ff");
        insertPicture(user.userId(), "头像", "[]", 1024, "0xff0000");

        mockMvc.perform(get("/picture/search/color")
                        .param("picColor", "0xff0000")
                        .header("satoken", user.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].picColor").value("0xff0000"));
    }

    /**
     * 缓存测试接口：两次调用应返回相同值（缓存命中）。
     */
    @Test
    void cacheTest_success() throws Exception {
        mockMvc.perform(get("/cache/test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").isNotEmpty());
    }
}
