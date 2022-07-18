package com.limited.sales.user;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.limited.sales.utils.JwtProperties;
import com.limited.sales.user.vo.User;
import com.limited.sales.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(value = "classpath:/application.properties")
@Transactional
@Slf4j
class UserControllerTest {
  @Autowired private WebApplicationContext context;

  @Autowired MockMvc mockMvc;

  private String JwtAccessToken;

  @BeforeEach
  public void setup() {
    mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();

    User user = User.builder().userEmail("test@test").userPassword("1234").build();

    JwtUtils utils = new JwtUtils();
    String accessTokenMethod = utils.createAccessToken(user);
    JwtAccessToken = JwtProperties.TOKEN_PREFIX + accessTokenMethod;
    log.info(JwtAccessToken);
  }

  @Test
  @Rollback
  void emailOverlapCheck() throws Exception {
    User user = User.builder().userEmail("ohjeung@naver.com").build();
    Gson gson = new Gson();
    String toJson = gson.toJson(user);
    log.info(toJson);

    mockMvc
        .perform(
            get("/user/email")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .characterEncoding("utf-8")
                .content(toJson))
        .andExpect(status().isOk())
        .andDo(print());
  }

  @Test
  @Rollback
  void signUp() throws Exception {
    User user =
        User.builder()
            .userEmail("ohejung@naver.com")
            .userPassword("1234")
            .useYn("Y")
            .userCellphone("01088887777")
            .userRole("")
            .build();

    Gson gson = new Gson();
    String jsonUser = gson.toJson(user);

    mockMvc
        .perform(
            post("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("utf-8")
                .content(jsonUser))
        .andExpect(status().isOk())
        .andDo(print());
  }

  @Test
  @Rollback
  void userInfoUpdate() throws Exception {
    User user = User.builder().userCellphone(null).build();

    Gson gson = new Gson();
    String jsonUser = gson.toJson(user);

    mockMvc
        .perform(
            patch("/user")
                .header(JwtProperties.HEADER_STRING, this.JwtAccessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("utf-8")
                .content(jsonUser))
        .andExpect(status().isBadRequest())
        .andDo(print());
  }

  @Test
  @Rollback
  void userPasswordChange() throws Exception {
    User user = User.builder().userEmail("test@test").userPassword("1234").build();

    Gson gson = new Gson();
    String jsonUser = gson.toJson(user);

    mockMvc
        .perform(
            patch("/user/password")
                .header(JwtProperties.HEADER_STRING, this.JwtAccessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("utf-8")
                .content(jsonUser))
        .andExpect(status().isOk())
        .andDo(print());
  }

  @Test
  @Rollback
  void userDelete() throws Exception {
    User user = User.builder().userEmail("test@test").userPassword("1234").build();

    Gson gson = new Gson();
    String jsonUser = gson.toJson(user);

    mockMvc
        .perform(
            delete("/user")
                .header(JwtProperties.HEADER_STRING, this.JwtAccessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("utf-8")
                .content(jsonUser))
        .andExpect(status().isOk())
        .andDo(print());
  }

  @Test
  @Rollback
  void admin() throws Exception {
    JsonObject jsonObject = new JsonObject();
    jsonObject.addProperty("userEmail", "test@test");
    jsonObject.addProperty("userPassword", "1234");

    mockMvc
        .perform(
            patch("/user/admin/limitedsale")
                .header(JwtProperties.HEADER_STRING, this.JwtAccessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("utf-8")
                .content(jsonObject.toString()))
        .andExpect(status().isOk())
        .andDo(print());
  }

  @Test
  void login() throws Exception {
    JsonObject jsonObject = new JsonObject();
    jsonObject.addProperty("userEmail", "ohjeung@naver.com");
    jsonObject.addProperty("userPassword", "1234");

    mockMvc
        .perform(
            post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("utf-8")
                .content(jsonObject.toString()))
        .andExpect(status().isOk())
        .andDo(print());
  }

  @Test
  @Rollback
  void logout() throws Exception {
    this.JwtAccessToken =
        "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJvaGpldW5nQG5hdmVyLmNvbSIsInVzZXJFbWFpbCI6Im9oamV1bmdAbmF2ZXIuY29tIiwiZXhwIjoxNjU4NDYwOTY0fQ.t3R0b4KVrzpCes6nbot1kEN2c9MM4xEarsCswHbQimPOd6vLO1QGbOor6cbWKIXf6V3eooLt7sxNERKUEEaKJQ";
    mockMvc
        .perform(
            post("/logout")
                .header(JwtProperties.HEADER_STRING, this.JwtAccessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("utf-8"))
        .andExpect(status().isOk())
        .andDo(print());
  }
}
