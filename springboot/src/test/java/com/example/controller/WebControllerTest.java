package com.example.controller;

import com.example.TestBase;
import com.example.entity.Account;
import com.example.entity.User;
import com.example.enums.RoleEnum;
import com.example.service.UserService;
import com.example.strategy.Context.RoleStrategyContext;
import com.example.strategy.RoleStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * WebController 单元测试类
 * 测试Web相关的REST API接口（登录、注册等）
 */
@DisplayName("WebController 单元测试")
class WebControllerTest extends TestBase {

    @Mock
    private RoleStrategyContext roleStrategyContext;

    @Mock
    private UserService userService;

    @Mock
    private RoleStrategy roleStrategy;

    @InjectMocks
    private WebController webController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        // 初始化MockMvc
        mockMvc = MockMvcBuilders.standaloneSetup(webController).build();
    }

    @Test
    @DisplayName("健康检查 - 成功")
    void testHello_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/hello"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(20000))
                .andExpect(jsonPath("$.message").value("hello"));
    }

    @Test
    @DisplayName("用户登录 - 成功（USER角色）")
    void testLogin_Success_User() throws Exception {
        // Given
        Account account = new Account();
        account.setUsername("testuser");
        account.setPassword("123456");
        account.setRole("USER");

        User mockUser = new User();
        mockUser.setId(1);
        mockUser.setUsername("testuser");
        mockUser.setRole("USER");

        when(roleStrategyContext.getStrategy("USER")).thenReturn(roleStrategy);
        when(roleStrategy.login(any(Account.class))).thenReturn(mockUser);

        // When & Then
        mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"testuser\",\"password\":\"123456\",\"role\":\"USER\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(20000));

        verify(roleStrategyContext, times(1)).getStrategy("USER");
        verify(roleStrategy, times(1)).login(any(Account.class));
    }

    @Test
    @DisplayName("用户登录 - 成功（SUPER_ADMIN角色）")
    void testLogin_Success_Admin() throws Exception {
        // Given
        Account account = new Account();
        account.setUsername("admin");
        account.setPassword("admin");
        account.setRole("SUPER_ADMIN");

        Account mockAdmin = new Account();
        mockAdmin.setId(1);
        mockAdmin.setUsername("admin");
        mockAdmin.setRole("SUPER_ADMIN");

        when(roleStrategyContext.getStrategy("SUPER_ADMIN")).thenReturn(roleStrategy);
        when(roleStrategy.login(any(Account.class))).thenReturn(mockAdmin);

        // When & Then
        mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"admin\",\"password\":\"123456\",\"role\":\"SUPER_ADMIN\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(20000));

        verify(roleStrategyContext, times(1)).getStrategy("SUPER_ADMIN");
        verify(roleStrategy, times(1)).login(any(Account.class));
    }

    @Test
    @DisplayName("用户注册 - 成功")
    void testRegister_Success() throws Exception {
        // Given
        doNothing().when(userService).register(any(User.class));

        // When & Then
        mockMvc.perform(post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"newuser\",\"password\":\"123456\",\"name\":\"新用户\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(20000))
                .andExpect(jsonPath("$.data").value("注册成功"));

        verify(userService, times(1)).register(any(User.class));
    }

    @Test
    @DisplayName("用户注册 - 强制设置角色为USER")
    void testRegister_ForceUserRole() throws Exception {
        // Given
        doNothing().when(userService).register(any(User.class));

        // When & Then - 即使传入其他角色，也应该被强制设置为USER
        mockMvc.perform(post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"newuser\",\"password\":\"123456\",\"role\":\"ADMIN\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(20000));

        // 验证userService.register被调用时，传入的User对象role为USER
        verify(userService, times(1)).register(argThat(user -> 
            user != null && RoleEnum.USER.getCode().equals(user.getRole())
        ));
    }

    @Test
    @DisplayName("更新密码 - 成功（USER角色）")
    void testUpdatePassword_Success_User() throws Exception {
        // Given
        when(roleStrategyContext.getStrategy("USER")).thenReturn(roleStrategy);
        doNothing().when(roleStrategy).updatePassword(any(Account.class));

        // When & Then
        mockMvc.perform(post("/updatePassword")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"password\":\"123456\",\"newpassword\":\"newpass123\",\"newPasswordConfirm\":\"newpass123\",\"role\":\"USER\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(20000));

        verify(roleStrategyContext, times(1)).getStrategy("USER");
        verify(roleStrategy, times(1)).updatePassword(any(Account.class));
    }

    @Test
    @DisplayName("更新密码 - 成功（SUPER_ADMIN角色）")
    void testUpdatePassword_Success_Admin() throws Exception {
        // Given
        when(roleStrategyContext.getStrategy("SUPER_ADMIN")).thenReturn(roleStrategy);
        doNothing().when(roleStrategy).updatePassword(any(Account.class));

        // When & Then
        mockMvc.perform(post("/updatePassword")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"password\":\"123456\",\"newpassword\":\"newpass123\",\"newPasswordConfirm\":\"newpass123\",\"role\":\"SUPER_ADMIN\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(20000));

        verify(roleStrategyContext, times(1)).getStrategy("SUPER_ADMIN");
        verify(roleStrategy, times(1)).updatePassword(any(Account.class));
    }

    @Test
    @DisplayName("用户登录 - 不同角色")
    void testLogin_DifferentRoles() throws Exception {
        // Given
        String[] roles = {"USER", "SUPER_ADMIN"};

        for (String role : roles) {
            Account mockAccount = new Account();
            mockAccount.setId(1);
            mockAccount.setUsername("test");
            mockAccount.setRole(role);

            when(roleStrategyContext.getStrategy(role)).thenReturn(roleStrategy);
            when(roleStrategy.login(any(Account.class))).thenReturn(mockAccount);

            // When & Then
            mockMvc.perform(post("/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"username\":\"test\",\"password\":\"123456\",\"role\":\"" + role + "\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(20000));

            verify(roleStrategyContext, times(1)).getStrategy(role);
        }
    }

    @Test
    @DisplayName("健康检查 - 多次调用")
    void testHello_MultipleCalls() throws Exception {
        // When & Then
        for (int i = 0; i < 3; i++) {
            mockMvc.perform(get("/hello"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(20000))
                    .andExpect(jsonPath("$.message").value("hello"));
        }
    }
}
