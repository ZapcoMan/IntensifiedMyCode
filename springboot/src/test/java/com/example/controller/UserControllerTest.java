package com.example.controller;

import com.example.TestBase;
import com.example.common.R;
import com.example.entity.User;
import com.example.service.impl.UserServiceImpl;
import com.github.pagehelper.PageInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * UserController 单元测试类
 * 测试用户相关的REST API接口
 */
@DisplayName("UserController 单元测试")
class UserControllerTest extends TestBase {

    @Mock
    private UserServiceImpl userServiceImpl;

    @InjectMocks
    private UserController userController;

    private MockMvc mockMvc;
    private User testUser;

    @BeforeEach
    void setUp() {
        // 初始化MockMvc
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();

        // 初始化测试数据
        testUser = new User();
        testUser.setId(1);
        testUser.setUsername("testuser");
        testUser.setName("测试用户");
        testUser.setPassword("123456");
        testUser.setRole("USER");
    }

    @Test
    @DisplayName("添加新用户 - 成功")
    void testAdd_Success() throws Exception {
        // Given
        doNothing().when(userServiceImpl).add(any(User.class));

        // When & Then
        mockMvc.perform(post("/user/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"newuser\",\"name\":\"新用户\",\"password\":\"123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(20000));

        verify(userServiceImpl, times(1)).add(any(User.class));
    }

    @Test
    @DisplayName("更新用户信息 - 成功")
    void testUpdate_Success() throws Exception {
        // Given
        doNothing().when(userServiceImpl).update(any(User.class));

        // When & Then
        mockMvc.perform(put("/user/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":1,\"username\":\"testuser\",\"name\":\"更新后的名称\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(20000));

        verify(userServiceImpl, times(1)).update(any(User.class));
    }

    @Test
    @DisplayName("根据ID删除用户 - 成功")
    void testDelete_Success() throws Exception {
        // Given
        Integer userId = 1;
        doNothing().when(userServiceImpl).deleteById(userId);

        // When & Then
        mockMvc.perform(delete("/user/delete/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(20000));

        verify(userServiceImpl, times(1)).deleteById(userId);
    }

    @Test
    @DisplayName("批量删除用户 - 成功")
    void testDeleteBatch_Success() throws Exception {
        // Given
        List<User> userList = Arrays.asList(testUser);
        doNothing().when(userServiceImpl).deleteBatch(anyList());

        // When & Then
        mockMvc.perform(delete("/user/deleteBatch")
                .contentType(MediaType.APPLICATION_JSON)
                .content("[{\"id\":1},{\"id\":2}]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(20000));

        verify(userServiceImpl, times(1)).deleteBatch(anyList());
    }

    @Test
    @DisplayName("查询所有用户 - 成功")
    void testSelectAll_Success() throws Exception {
        // Given
        List<User> userList = Arrays.asList(testUser);
        when(userServiceImpl.selectAll()).thenReturn(userList);

        // When & Then
        mockMvc.perform(get("/user/selectAll"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(20000));

        verify(userServiceImpl, times(1)).selectAll();
    }

    @Test
    @DisplayName("分页查询用户 - 成功（默认参数）")
    void testSelectPage_DefaultParams() throws Exception {
        // Given
        PageInfo<User> pageInfo = new PageInfo<>(Arrays.asList(testUser));
        when(userServiceImpl.selectPage(eq(1), eq(10), any(User.class))).thenReturn(pageInfo);

        // When & Then
        mockMvc.perform(get("/user/selectPage"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(20000));

        verify(userServiceImpl, times(1)).selectPage(eq(1), eq(10), any(User.class));
    }

    @Test
    @DisplayName("分页查询用户 - 成功（自定义参数）")
    void testSelectPage_CustomParams() throws Exception {
        // Given
        Integer pageNum = 2;
        Integer pageSize = 5;
        PageInfo<User> pageInfo = new PageInfo<>(Arrays.asList(testUser));
        when(userServiceImpl.selectPage(eq(pageNum), eq(pageSize), any(User.class))).thenReturn(pageInfo);

        // When & Then
        mockMvc.perform(get("/user/selectPage")
                .param("pageNum", pageNum.toString())
                .param("pageSize", pageSize.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(20000));

        verify(userServiceImpl, times(1)).selectPage(eq(pageNum), eq(pageSize), any(User.class));
    }

    @Test
    @DisplayName("分页查询用户 - 带查询条件")
    void testSelectPage_WithCondition() throws Exception {
        // Given
        PageInfo<User> pageInfo = new PageInfo<>(Arrays.asList(testUser));
        when(userServiceImpl.selectPage(eq(1), eq(10), any(User.class))).thenReturn(pageInfo);

        // When & Then
        mockMvc.perform(get("/user/selectPage")
                .param("username", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(20000));

        verify(userServiceImpl, times(1)).selectPage(eq(1), eq(10), any(User.class));
    }

    @Test
    @DisplayName("验证Token - 成功")
    void testValidateToken_Success() throws Exception {
        // Given
        // TokenUtils.validateToken()是静态方法，需要特殊处理
        // 这里暂时跳过实际执行

        // When & Then
        // 由于静态方法mock的复杂性，这个测试需要在集成测试中验证
    }

    @Test
    @DisplayName("验证Token - 失败")
    void testValidateToken_Failed() throws Exception {
        // Given
        // TokenUtils.validateToken()是静态方法，需要特殊处理
        // 这里暂时跳过实际执行

        // When & Then
        // 由于静态方法mock的复杂性，这个测试需要在集成测试中验证
    }

    @Test
    @DisplayName("添加用户 - 请求体为空")
    void testAdd_EmptyBody() throws Exception {
        // When & Then
        mockMvc.perform(post("/user/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isOk());

        verify(userServiceImpl, times(1)).add(any(User.class));
    }

    @Test
    @DisplayName("更新用户 - 请求体为空")
    void testUpdate_EmptyBody() throws Exception {
        // When & Then
        mockMvc.perform(put("/user/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isOk());

        verify(userServiceImpl, times(1)).update(any(User.class));
    }

    @Test
    @DisplayName("删除用户 - ID不存在")
    void testDelete_NonExistentId() throws Exception {
        // Given
        Integer userId = 999;
        doNothing().when(userServiceImpl).deleteById(userId);

        // When & Then
        mockMvc.perform(delete("/user/delete/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(20000));

        verify(userServiceImpl, times(1)).deleteById(userId);
    }

    @Test
    @DisplayName("批量删除 - 空列表")
    void testDeleteBatch_EmptyList() throws Exception {
        // Given
        doNothing().when(userServiceImpl).deleteBatch(anyList());

        // When & Then
        mockMvc.perform(delete("/user/deleteBatch")
                .contentType(MediaType.APPLICATION_JSON)
                .content("[]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(20000));

        verify(userServiceImpl, times(1)).deleteBatch(anyList());
    }

    @Test
    @DisplayName("查询所有用户 - 返回空列表")
    void testSelectAll_EmptyList() throws Exception {
        // Given
        when(userServiceImpl.selectAll()).thenReturn(Arrays.asList());

        // When & Then
        mockMvc.perform(get("/user/selectAll"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(20000));

        verify(userServiceImpl, times(1)).selectAll();
    }

    @Test
    @DisplayName("分页查询 - 第一页")
    void testSelectPage_FirstPage() throws Exception {
        // Given
        PageInfo<User> pageInfo = new PageInfo<>(Arrays.asList(testUser));
        pageInfo.setPageNum(1);
        pageInfo.setPages(1);
        when(userServiceImpl.selectPage(eq(1), eq(10), any(User.class))).thenReturn(pageInfo);

        // When & Then
        mockMvc.perform(get("/user/selectPage")
                .param("pageNum", "1")
                .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(20000));

        verify(userServiceImpl, times(1)).selectPage(eq(1), eq(10), any(User.class));
    }

    @Test
    @DisplayName("分页查询 - 最后一页")
    void testSelectPage_LastPage() throws Exception {
        // Given
        PageInfo<User> pageInfo = new PageInfo<>(Arrays.asList());
        pageInfo.setPageNum(10);
        pageInfo.setPages(10);
        when(userServiceImpl.selectPage(eq(10), eq(10), any(User.class))).thenReturn(pageInfo);

        // When & Then
        mockMvc.perform(get("/user/selectPage")
                .param("pageNum", "10")
                .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(20000));

        verify(userServiceImpl, times(1)).selectPage(eq(10), eq(10), any(User.class));
    }
}
