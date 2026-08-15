package com.ylum.cloudgallery.manager.sharding;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ylum.cloudgallery.model.dto.SpaceAddRequest;
import com.ylum.cloudgallery.model.dto.UserLoginRequest;
import com.ylum.cloudgallery.model.dto.UserRegisterRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import javax.imageio.ImageIO;
import javax.sql.DataSource;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 分库分表端到端集成测试：验证「创建空间 → 自动建表 → 图片写入路由到分表 → 查询路由」完整链路。
 *
 * <p>与普通集成测试不同，本测试 <b>不开启 {@code @Transactional}</b>：因为建表是 DDL（隐式提交，
 * 无法回滚），且需要从底层物理数据源用独立连接核对「数据真实落在哪张物理表」，只有提交后才能被
 * 独立连接看到。测试数据与分表在 {@link #cleanUp()} 中手动清理。</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
class PictureShardingEndToEndTest {

    private static final String PASSWORD = "12345678";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    /** MySQL 底层物理数据源：绕过 ShardingSphere，直接核对物理分表与数据落位 */
    @Autowired
    @Qualifier("mysqlDataSource")
    private DataSource physicalDataSource;

    /** 记录测试创建的用户 / 空间，供 {@link #cleanUp()} 清理 */
    private Long createdUserId;
    private Long createdSpaceId;

    /**
     * 清理测试产生的物理分表与业务记录（DDL 无法回滚，需手动清理）。
     */
    @AfterEach
    void cleanUp() throws Exception {
        try (Connection connection = physicalDataSource.getConnection();
             Statement statement = connection.createStatement()) {
            if (createdSpaceId != null) {
                // 图片记录随分表一并删除
                statement.executeUpdate("DROP TABLE IF EXISTS picture_" + createdSpaceId);
                statement.executeUpdate("DELETE FROM space WHERE id = " + createdSpaceId);
            }
            if (createdUserId != null) {
                statement.executeUpdate("DELETE FROM user WHERE id = " + createdUserId);
            }
        }
    }

    /**
     * 注册并登录，返回 token（用户记录真实提交，由 cleanUp 清理）。
     */
    private String registerAndLogin(String account) throws Exception {
        UserRegisterRequest register = new UserRegisterRequest();
        register.setUserAccount(account);
        register.setUserPassword(PASSWORD);
        register.setCheckPassword(PASSWORD);
        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isOk());

        UserLoginRequest login = new UserLoginRequest();
        login.setUserAccount(account);
        login.setUserPassword(PASSWORD);
        String body = mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode data = objectMapper.readTree(body).path("data");
        createdUserId = data.path("id").asLong();
        return data.path("token").asText();
    }

    /**
     * 生成唯一账号，避免测试间冲突。
     */
    private String uniqueAccount() {
        return "shard_e2e_" + System.nanoTime() + "_" + (int) (Math.random() * 100000);
    }

    /**
     * 创建团队空间，返回空间 ID。
     */
    private long createTeamSpace(String token) throws Exception {
        SpaceAddRequest request = new SpaceAddRequest();
        request.setSpaceName("分表端到端测试空间");
        request.setSpaceType(1);

        String body = mockMvc.perform(post("/space/add")
                        .header("satoken", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn().getResponse().getContentAsString();
        createdSpaceId = objectMapper.readTree(body).path("data").asLong();
        return createdSpaceId;
    }

    /**
     * 查询物理表是否存在于当前数据库。
     */
    private boolean tableExists(String tableName) throws Exception {
        try (Connection connection = physicalDataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(
                     "SELECT COUNT(*) FROM information_schema.tables "
                             + "WHERE table_schema = DATABASE() AND table_name = '" + tableName + "'")) {
            return resultSet.next() && resultSet.getInt(1) > 0;
        }
    }

    /**
     * 查询指定物理表中某空间下的图片数量。
     */
    private int countBySpace(String tableName, long spaceId) throws Exception {
        try (Connection connection = physicalDataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(
                     "SELECT COUNT(*) FROM " + tableName + " WHERE space_id = " + spaceId)) {
            return resultSet.next() ? resultSet.getInt(1) : 0;
        }
    }

    /**
     * 生成一张 100x100 纯色 PNG 测试图片。
     */
    private MockMultipartFile createTestImage(String name) throws Exception {
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(Color.BLUE);
        graphics.fillRect(0, 0, 100, 100);
        graphics.dispose();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, "png", out);
        return new MockMultipartFile("file", name, "image/png", out.toByteArray());
    }

    /**
     * 端到端验证：创建空间自动建表，图片写入 / 查询正确路由到分表，且与公共图库隔离。
     */
    @Test
    void 创建空间自动建表_图片写入查询正确路由到分表() throws Exception {
        String token = registerAndLogin(uniqueAccount());

        // 1. 创建团队空间 → 自动建表
        long spaceId = createTeamSpace(token);
        assertTrue(tableExists("picture_" + spaceId), "创建空间后应自动生成分表 picture_" + spaceId);

        // 2. 上传图片到空间 → 写入应路由到分表
        mockMvc.perform(multipart("/picture/upload")
                        .file(createTestImage("shard.png"))
                        .param("spaceId", String.valueOf(spaceId))
                        .header("satoken", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").isNumber());

        // 3. 核对物理落位：分表有该空间图片，公共 picture 表没有
        assertEquals(1, countBySpace("picture_" + spaceId, spaceId), "图片应写入分表 picture_" + spaceId);
        assertEquals(0, countBySpace("picture", spaceId), "公共图库不应有该空间图片");

        // 4. 查询路由：空间查询命中分表，公共图库查询隔离
        mockMvc.perform(get("/picture/list/page/vo")
                        .param("spaceId", String.valueOf(spaceId))
                        .param("current", "1")
                        .param("pageSize", "10")
                        .header("satoken", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(1));

        mockMvc.perform(get("/picture/list/page/vo")
                        .param("current", "1")
                        .param("pageSize", "10")
                        .header("satoken", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(0));
    }
}
