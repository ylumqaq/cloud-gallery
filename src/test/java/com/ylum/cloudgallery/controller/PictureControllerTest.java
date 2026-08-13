package com.ylum.cloudgallery.controller;

import com.ylum.cloudgallery.BaseIntegrationTest;
import com.ylum.cloudgallery.model.dto.PictureDeleteRequest;
import com.ylum.cloudgallery.model.dto.PictureEditRequest;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 图片模块接口集成测试：上传 / 分页查询 / 详情 / 编辑 / 删除。
 *
 * <p>上传会真实写入腾讯云 COS（测试事务仅回滚 MySQL，COS 对象为真实副作用）。
 * 需在 application.yml 配置真实 COS 密钥后方可运行。</p>
 */
class PictureControllerTest extends BaseIntegrationTest {

    /**
     * 生成一张 100x100 的纯色 PNG 测试图片。
     */
    private MockMultipartFile createTestImage(String name) throws Exception {
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(Color.RED);
        graphics.fillRect(0, 0, 100, 100);
        graphics.dispose();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, "png", out);
        return new MockMultipartFile("file", name, "image/png", out.toByteArray());
    }

    /**
     * 上传图片到公共图库（spaceId 为空），返回图片 ID。
     */
    private long uploadPicture(String token, MockMultipartFile file, Long spaceId) throws Exception {
        MockHttpServletRequestBuilder builder = multipart("/picture/upload")
                .file(file)
                .header("satoken", token);
        if (spaceId != null) {
            builder.param("spaceId", String.valueOf(spaceId));
        }
        String body = mockMvc.perform(builder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).path("data").path("id").asLong();
    }

    /**
     * 本地上传公共图库成功，返回原图与缩略图地址。
     */
    @Test
    void uploadPicture_publicGallery_success() throws Exception {
        String token = registerAndLogin(uniqueAccount()).token();

        mockMvc.perform(multipart("/picture/upload")
                        .file(createTestImage("test.png"))
                        .header("satoken", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.url").isNotEmpty())
                .andExpect(jsonPath("$.data.thumbnailUrl").isNotEmpty());
    }

    /**
     * 未登录上传应返回 40100。
     */
    @Test
    void uploadPicture_notLogin_returnsNotLogin() throws Exception {
        mockMvc.perform(multipart("/picture/upload")
                        .file(createTestImage("test.png")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40100));
    }

    /**
     * 上传后分页查询公共图库，应能查到图片。
     */
    @Test
    void listPictureByPage_publicGallery_success() throws Exception {
        String token = registerAndLogin(uniqueAccount()).token();
        uploadPicture(token, createTestImage("a.png"), null);
        uploadPicture(token, createTestImage("b.png"), null);

        mockMvc.perform(get("/picture/list/page/vo")
                        .param("current", "1")
                        .param("pageSize", "10")
                        .header("satoken", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.total").value(2));
    }

    /**
     * 上传后查询详情，应返回对应图片信息。
     */
    @Test
    void getPictureById_success() throws Exception {
        String token = registerAndLogin(uniqueAccount()).token();
        long pictureId = uploadPicture(token, createTestImage("detail.png"), null);

        mockMvc.perform(get("/picture/get/{id}", pictureId)
                        .header("satoken", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").value(pictureId));
    }

    /**
     * 上传后编辑名称，详情应能查到新名称。
     */
    @Test
    void editPicture_success() throws Exception {
        String token = registerAndLogin(uniqueAccount()).token();
        long pictureId = uploadPicture(token, createTestImage("edit.png"), null);

        PictureEditRequest request = new PictureEditRequest();
        request.setId(pictureId);
        request.setName("新名称");

        mockMvc.perform(post("/picture/edit")
                        .header("satoken", token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(get("/picture/get/{id}", pictureId)
                        .header("satoken", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("新名称"));
    }

    /**
     * 上传后删除，详情应返回 40400。
     */
    @Test
    void deletePicture_success() throws Exception {
        String token = registerAndLogin(uniqueAccount()).token();
        long pictureId = uploadPicture(token, createTestImage("delete.png"), null);

        PictureDeleteRequest request = new PictureDeleteRequest();
        request.setId(pictureId);

        mockMvc.perform(post("/picture/delete")
                        .header("satoken", token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(get("/picture/get/{id}", pictureId)
                        .header("satoken", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40400));
    }

    /**
     * URL 上传成功（依赖外网图片源）。
     */
    @Test
    void uploadPicture_url_success() throws Exception {
        String token = registerAndLogin(uniqueAccount()).token();

        mockMvc.perform(multipart("/picture/upload")
                        .param("fileUrl", "https://picsum.photos/200/200")
                        .header("satoken", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.url").isNotEmpty());
    }
}
