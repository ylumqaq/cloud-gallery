package com.ylum.cloudgallery.controller;

import com.ylum.cloudgallery.BaseIntegrationTest;
import com.ylum.cloudgallery.constant.SpaceUserConstant;
import com.ylum.cloudgallery.model.dto.SpaceAddRequest;
import com.ylum.cloudgallery.model.dto.SpaceUserAddRequest;
import com.ylum.cloudgallery.model.dto.SpaceUserEditRequest;
import com.ylum.cloudgallery.model.dto.SpaceUserRemoveRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 空间成员模块接口测试：添加 / 修改角色 / 移除 / 列表 / 当前用户权限，以及权限边界。
 */
class SpaceUserControllerTest extends BaseIntegrationTest {

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

    /**
     * 添加成员（期望成功）。
     */
    private void addSpaceUser(String token, long spaceId, long userId, String role) throws Exception {
        SpaceUserAddRequest request = new SpaceUserAddRequest();
        request.setSpaceId(spaceId);
        request.setUserId(userId);
        request.setSpaceRole(role);
        mockMvc.perform(post("/spaceUser/add")
                        .header("satoken", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void addSpaceUser_creator_success() throws Exception {
        AuthUser owner = registerAndLogin(uniqueAccount());
        long spaceId = createSpace(owner.token(), "团队空间", 1);
        AuthUser member = registerAndLogin(uniqueAccount());

        SpaceUserAddRequest request = new SpaceUserAddRequest();
        request.setSpaceId(spaceId);
        request.setUserId(member.userId());
        request.setSpaceRole(SpaceUserConstant.ROLE_VIEWER);

        mockMvc.perform(post("/spaceUser/add")
                        .header("satoken", owner.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").isNumber());
    }

    @Test
    void addSpaceUser_invalidRole_returnsParamsError() throws Exception {
        AuthUser owner = registerAndLogin(uniqueAccount());
        long spaceId = createSpace(owner.token(), "团队空间", 1);
        AuthUser member = registerAndLogin(uniqueAccount());

        SpaceUserAddRequest request = new SpaceUserAddRequest();
        request.setSpaceId(spaceId);
        request.setUserId(member.userId());
        request.setSpaceRole("boss");

        mockMvc.perform(post("/spaceUser/add")
                        .header("satoken", owner.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40000));
    }

    @Test
    void addSpaceUser_duplicate_returnsParamsError() throws Exception {
        AuthUser owner = registerAndLogin(uniqueAccount());
        long spaceId = createSpace(owner.token(), "团队空间", 1);
        AuthUser member = registerAndLogin(uniqueAccount());
        addSpaceUser(owner.token(), spaceId, member.userId(), SpaceUserConstant.ROLE_VIEWER);

        SpaceUserAddRequest request = new SpaceUserAddRequest();
        request.setSpaceId(spaceId);
        request.setUserId(member.userId());
        request.setSpaceRole(SpaceUserConstant.ROLE_EDITOR);

        mockMvc.perform(post("/spaceUser/add")
                        .header("satoken", owner.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40000));
    }

    @Test
    void editSpaceUser_success() throws Exception {
        AuthUser owner = registerAndLogin(uniqueAccount());
        long spaceId = createSpace(owner.token(), "团队空间", 1);
        AuthUser member = registerAndLogin(uniqueAccount());
        addSpaceUser(owner.token(), spaceId, member.userId(), SpaceUserConstant.ROLE_VIEWER);

        SpaceUserEditRequest request = new SpaceUserEditRequest();
        request.setSpaceId(spaceId);
        request.setUserId(member.userId());
        request.setSpaceRole(SpaceUserConstant.ROLE_EDITOR);

        mockMvc.perform(post("/spaceUser/edit")
                        .header("satoken", owner.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void removeSpaceUser_success() throws Exception {
        AuthUser owner = registerAndLogin(uniqueAccount());
        long spaceId = createSpace(owner.token(), "团队空间", 1);
        AuthUser member = registerAndLogin(uniqueAccount());
        addSpaceUser(owner.token(), spaceId, member.userId(), SpaceUserConstant.ROLE_VIEWER);

        SpaceUserRemoveRequest request = new SpaceUserRemoveRequest();
        request.setSpaceId(spaceId);
        request.setUserId(member.userId());

        mockMvc.perform(post("/spaceUser/delete")
                        .header("satoken", owner.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void listSpaceUsers_success() throws Exception {
        AuthUser owner = registerAndLogin(uniqueAccount());
        long spaceId = createSpace(owner.token(), "团队空间", 1);
        AuthUser member = registerAndLogin(uniqueAccount());
        addSpaceUser(owner.token(), spaceId, member.userId(), SpaceUserConstant.ROLE_VIEWER);

        mockMvc.perform(get("/spaceUser/list")
                        .param("spaceId", String.valueOf(spaceId))
                        .header("satoken", owner.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    void getCurrentUserAuth_creator_returnsRoleAndPermissions() throws Exception {
        AuthUser owner = registerAndLogin(uniqueAccount());
        long spaceId = createSpace(owner.token(), "团队空间", 1);

        mockMvc.perform(get("/spaceUser/get")
                        .param("spaceId", String.valueOf(spaceId))
                        .header("satoken", owner.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.role").value("creator"))
                .andExpect(jsonPath("$.data.permissions").isArray());
    }

    @Test
    void addSpaceUser_viewer_returnsNoAuth() throws Exception {
        AuthUser owner = registerAndLogin(uniqueAccount());
        long spaceId = createSpace(owner.token(), "团队空间", 1);

        AuthUser viewer = registerAndLogin(uniqueAccount());
        addSpaceUser(owner.token(), spaceId, viewer.userId(), SpaceUserConstant.ROLE_VIEWER);

        AuthUser victim = registerAndLogin(uniqueAccount());
        SpaceUserAddRequest request = new SpaceUserAddRequest();
        request.setSpaceId(spaceId);
        request.setUserId(victim.userId());
        request.setSpaceRole(SpaceUserConstant.ROLE_EDITOR);

        mockMvc.perform(post("/spaceUser/add")
                        .header("satoken", viewer.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40101));
    }

    @Test
    void addSpaceUser_editor_returnsNoAuth() throws Exception {
        AuthUser owner = registerAndLogin(uniqueAccount());
        long spaceId = createSpace(owner.token(), "团队空间", 1);

        AuthUser editor = registerAndLogin(uniqueAccount());
        addSpaceUser(owner.token(), spaceId, editor.userId(), SpaceUserConstant.ROLE_EDITOR);

        AuthUser victim = registerAndLogin(uniqueAccount());
        SpaceUserAddRequest request = new SpaceUserAddRequest();
        request.setSpaceId(spaceId);
        request.setUserId(victim.userId());
        request.setSpaceRole(SpaceUserConstant.ROLE_VIEWER);

        mockMvc.perform(post("/spaceUser/add")
                        .header("satoken", editor.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40101));
    }

    @Test
    void listSpaceUsers_privateSpace_nonCreator_returnsNoAuth() throws Exception {
        AuthUser owner = registerAndLogin(uniqueAccount());
        long spaceId = createSpace(owner.token(), "私有空间", 0);

        AuthUser other = registerAndLogin(uniqueAccount());

        mockMvc.perform(get("/spaceUser/list")
                        .param("spaceId", String.valueOf(spaceId))
                        .header("satoken", other.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40101));
    }
}
