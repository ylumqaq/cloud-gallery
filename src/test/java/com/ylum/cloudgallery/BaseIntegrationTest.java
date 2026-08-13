package com.ylum.cloudgallery;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ylum.cloudgallery.model.dto.UserLoginRequest;
import com.ylum.cloudgallery.model.dto.UserRegisterRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 接口集成测试基类。
 *
 * <p>基于真实 Spring 上下文 + MockMvc 发起请求，方法运行在事务中并自动回滚，
 * 避免污染数据库。context-path（/api）属于 Servlet 容器层，MockMvc 请求路径无需携带。</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public abstract class BaseIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    /** 默认测试密码 */
    protected static final String PASSWORD = "12345678";

    /**
     * 登录用户信息：用户 ID + token。
     */
    protected record AuthUser(long userId, String token) {
    }

    /**
     * 注册并登录，返回用户 ID 与 token。
     */
    protected AuthUser registerAndLogin(String account) throws Exception {
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
        long userId = data.path("id").asLong();
        String token = data.path("token").asText();
        return new AuthUser(userId, token);
    }

    /**
     * 生成唯一账号，避免测试间数据冲突。
     */
    protected String uniqueAccount() {
        return "u_" + System.nanoTime() + "_" + (int) (Math.random() * 100000);
    }
}
