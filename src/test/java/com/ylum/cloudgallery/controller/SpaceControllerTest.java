package com.ylum.cloudgallery.controller;

import com.ylum.cloudgallery.BaseIntegrationTest;
import com.ylum.cloudgallery.model.dto.SpaceAddRequest;
import com.ylum.cloudgallery.model.dto.SpaceDeleteRequest;
import com.ylum.cloudgallery.model.dto.SpaceEditRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 空间模块接口测试：创建 / 编辑 / 删除 / 详情 / 列表。
 */
class SpaceControllerTest extends BaseIntegrationTest {

    /**
     * 创建空间，返回空间 ID。
     */
    private long createSpace(String token, String spaceName, int spaceType) throws Exception {
        SpaceAddRequest request = new SpaceAddRequest();
        request.setSpaceName(spaceName);
        request.setSpaceType(spaceType);
        String body = mockMvc.perform(post("/space/add")
                        .header("satoken", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).path("data").asLong();
    }

    @Test
    void addSpace_success() throws Exception {
        String token = registerAndLogin(uniqueAccount()).token();

        SpaceAddRequest request = new SpaceAddRequest();
        request.setSpaceName("我的空间");
        request.setSpaceType(0);

        mockMvc.perform(post("/space/add")
                        .header("satoken", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").isNumber());
    }

    @Test
    void addSpace_invalidType_returnsParamsError() throws Exception {
        String token = registerAndLogin(uniqueAccount()).token();

        SpaceAddRequest request = new SpaceAddRequest();
        request.setSpaceName("非法类型");
        request.setSpaceType(99);

        mockMvc.perform(post("/space/add")
                        .header("satoken", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40000));
    }

    @Test
    void editSpace_creator_success() throws Exception {
        String token = registerAndLogin(uniqueAccount()).token();
        long spaceId = createSpace(token, "空间A", 0);

        SpaceEditRequest request = new SpaceEditRequest();
        request.setId(spaceId);
        request.setSpaceName("新名称");

        mockMvc.perform(post("/space/edit")
                        .header("satoken", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void editSpace_nonCreator_returnsNoAuth() throws Exception {
        String ownerToken = registerAndLogin(uniqueAccount()).token();
        long spaceId = createSpace(ownerToken, "空间A", 0);

        String otherToken = registerAndLogin(uniqueAccount()).token();
        SpaceEditRequest request = new SpaceEditRequest();
        request.setId(spaceId);
        request.setSpaceName("篡改");

        mockMvc.perform(post("/space/edit")
                        .header("satoken", otherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40101));
    }

    @Test
    void deleteSpace_creator_success() throws Exception {
        String token = registerAndLogin(uniqueAccount()).token();
        long spaceId = createSpace(token, "空间A", 0);

        SpaceDeleteRequest request = new SpaceDeleteRequest();
        request.setId(spaceId);

        mockMvc.perform(post("/space/delete")
                        .header("satoken", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void deleteSpace_nonCreator_returnsNoAuth() throws Exception {
        String ownerToken = registerAndLogin(uniqueAccount()).token();
        long spaceId = createSpace(ownerToken, "空间A", 0);

        String otherToken = registerAndLogin(uniqueAccount()).token();
        SpaceDeleteRequest request = new SpaceDeleteRequest();
        request.setId(spaceId);

        mockMvc.perform(post("/space/delete")
                        .header("satoken", otherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40101));
    }

    @Test
    void getSpaceById_success() throws Exception {
        String token = registerAndLogin(uniqueAccount()).token();
        long spaceId = createSpace(token, "空间A", 0);

        mockMvc.perform(get("/space/get/{id}", spaceId)
                        .header("satoken", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").value(spaceId))
                .andExpect(jsonPath("$.data.spaceName").value("空间A"));
    }

    @Test
    void listMySpaces_success() throws Exception {
        String token = registerAndLogin(uniqueAccount()).token();
        createSpace(token, "空间A", 0);
        createSpace(token, "空间B", 1);

        mockMvc.perform(get("/space/list")
                        .header("satoken", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));
    }
}
