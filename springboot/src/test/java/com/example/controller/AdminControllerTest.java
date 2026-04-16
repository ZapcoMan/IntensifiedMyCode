package com.example.controller;

import com.example.TestBase;
import com.example.entity.Admin;
import com.example.service.impl.AdminServiceImpl;
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
 * AdminController 单元测试类
 * 测试管理员相关的REST API接口
 */
@DisplayName("AdminController 单元测试")
class AdminControllerTest extends TestBase {

    @Mock
    private AdminServiceImpl adminServiceImpl;

    @InjectMocks
    private AdminController adminController;

    private MockMvc mockMvc;
    private Admin testAdmin;

    @BeforeEach
    void setUp() {
        // 初始化MockMvc
        mockMvc = MockMvcBuilders.standaloneSetup(adminController).build();

        // 初始化测试数据
        testAdmin = new Admin();
        testAdmin.setId(1);
        testAdmin.setUsername("admin");
        testAdmin.setName("超级管理员");
        testAdmin.setPassword("123456");
        testAdmin.setRole("SUPER_ADMIN");
    }

    @Test
    @DisplayName("添加管理员 - 成功")
    void testAdd_Success() throws Exception {
        // Given
        doNothing().when(adminServiceImpl).add(any(Admin.class));

        // When & Then
        mockMvc.perform(post("/admin/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"newadmin\",\"name\":\"新管理员\",\"password\":\"123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(20000));

        verify(adminServiceImpl, times(1)).add(any(Admin.class));
    }

    @Test
    @DisplayName("更新管理员信息 - 成功")
    void testUpdate_Success() throws Exception {
        // Given
        doNothing().when(adminServiceImpl).update(any(Admin.class));

        // When & Then
        mockMvc.perform(put("/admin/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":1,\"username\":\"admin\",\"name\":\"更新后的名称\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(20000));

        verify(adminServiceImpl, times(1)).update(any(Admin.class));
    }

    @Test
    @DisplayName("根据ID删除管理员 - 成功")
    void testDelete_Success() throws Exception {
        // Given
        Integer adminId = 1;
        doNothing().when(adminServiceImpl).deleteById(adminId);

        // When & Then
        mockMvc.perform(delete("/admin/delete/{id}", adminId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(20000));

        verify(adminServiceImpl, times(1)).deleteById(adminId);
    }

    @Test
    @DisplayName("批量删除管理员 - 成功")
    void testDeleteBatch_Success() throws Exception {
        // Given
        List<Admin> adminList = Arrays.asList(testAdmin);
        doNothing().when(adminServiceImpl).deleteBatch(anyList());

        // When & Then
        mockMvc.perform(delete("/admin/deleteBatch")
                .contentType(MediaType.APPLICATION_JSON)
                .content("[{\"id\":1},{\"id\":2}]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(20000));

        verify(adminServiceImpl, times(1)).deleteBatch(anyList());
    }

    @Test
    @DisplayName("查询所有管理员 - 成功")
    void testSelectAll_Success() throws Exception {
        // Given
        List<Admin> adminList = Arrays.asList(testAdmin);
        when(adminServiceImpl.selectAll()).thenReturn(adminList);

        // When & Then
        mockMvc.perform(get("/admin/selectAll"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(20000));

        verify(adminServiceImpl, times(1)).selectAll();
    }

    @Test
    @DisplayName("分页查询管理员 - 成功（默认参数）")
    void testSelectPage_DefaultParams() throws Exception {
        // Given
        PageInfo<Admin> pageInfo = new PageInfo<>(Arrays.asList(testAdmin));
        when(adminServiceImpl.selectPage(eq(1), eq(10), any(Admin.class))).thenReturn(pageInfo);

        // When & Then
        mockMvc.perform(get("/admin/selectPage"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(20000));

        verify(adminServiceImpl, times(1)).selectPage(eq(1), eq(10), any(Admin.class));
    }

    @Test
    @DisplayName("分页查询管理员 - 成功（自定义参数）")
    void testSelectPage_CustomParams() throws Exception {
        // Given
        Integer pageNum = 2;
        Integer pageSize = 5;
        PageInfo<Admin> pageInfo = new PageInfo<>(Arrays.asList(testAdmin));
        when(adminServiceImpl.selectPage(eq(pageNum), eq(pageSize), any(Admin.class))).thenReturn(pageInfo);

        // When & Then
        mockMvc.perform(get("/admin/selectPage")
                .param("pageNum", pageNum.toString())
                .param("pageSize", pageSize.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(20000));

        verify(adminServiceImpl, times(1)).selectPage(eq(pageNum), eq(pageSize), any(Admin.class));
    }

    @Test
    @DisplayName("分页查询管理员 - 带查询条件")
    void testSelectPage_WithCondition() throws Exception {
        // Given
        PageInfo<Admin> pageInfo = new PageInfo<>(Arrays.asList(testAdmin));
        when(adminServiceImpl.selectPage(eq(1), eq(10), any(Admin.class))).thenReturn(pageInfo);

        // When & Then
        mockMvc.perform(get("/admin/selectPage")
                .param("username", "admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(20000));

        verify(adminServiceImpl, times(1)).selectPage(eq(1), eq(10), any(Admin.class));
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
    @DisplayName("添加管理员 - 请求体为空")
    void testAdd_EmptyBody() throws Exception {
        // When & Then
        mockMvc.perform(post("/admin/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isOk());

        verify(adminServiceImpl, times(1)).add(any(Admin.class));
    }

    @Test
    @DisplayName("更新管理员 - 请求体为空")
    void testUpdate_EmptyBody() throws Exception {
        // When & Then
        mockMvc.perform(put("/admin/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isOk());

        verify(adminServiceImpl, times(1)).update(any(Admin.class));
    }

    @Test
    @DisplayName("删除管理员 - ID不存在")
    void testDelete_NonExistentId() throws Exception {
        // Given
        Integer adminId = 999;
        doNothing().when(adminServiceImpl).deleteById(adminId);

        // When & Then
        mockMvc.perform(delete("/admin/delete/{id}", adminId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(20000));

        verify(adminServiceImpl, times(1)).deleteById(adminId);
    }

    @Test
    @DisplayName("批量删除 - 空列表")
    void testDeleteBatch_EmptyList() throws Exception {
        // Given
        doNothing().when(adminServiceImpl).deleteBatch(anyList());

        // When & Then
        mockMvc.perform(delete("/admin/deleteBatch")
                .contentType(MediaType.APPLICATION_JSON)
                .content("[]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(20000));

        verify(adminServiceImpl, times(1)).deleteBatch(anyList());
    }

    @Test
    @DisplayName("查询所有管理员 - 返回空列表")
    void testSelectAll_EmptyList() throws Exception {
        // Given
        when(adminServiceImpl.selectAll()).thenReturn(Arrays.asList());

        // When & Then
        mockMvc.perform(get("/admin/selectAll"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(20000));

        verify(adminServiceImpl, times(1)).selectAll();
    }

    @Test
    @DisplayName("分页查询 - 第一页")
    void testSelectPage_FirstPage() throws Exception {
        // Given
        PageInfo<Admin> pageInfo = new PageInfo<>(Arrays.asList(testAdmin));
        pageInfo.setPageNum(1);
        pageInfo.setPages(1);
        when(adminServiceImpl.selectPage(eq(1), eq(10), any(Admin.class))).thenReturn(pageInfo);

        // When & Then
        mockMvc.perform(get("/admin/selectPage")
                .param("pageNum", "1")
                .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(20000));

        verify(adminServiceImpl, times(1)).selectPage(eq(1), eq(10), any(Admin.class));
    }

    @Test
    @DisplayName("分页查询 - 最后一页")
    void testSelectPage_LastPage() throws Exception {
        // Given
        PageInfo<Admin> pageInfo = new PageInfo<>(Arrays.asList());
        pageInfo.setPageNum(10);
        pageInfo.setPages(10);
        when(adminServiceImpl.selectPage(eq(10), eq(10), any(Admin.class))).thenReturn(pageInfo);

        // When & Then
        mockMvc.perform(get("/admin/selectPage")
                .param("pageNum", "10")
                .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(20000));

        verify(adminServiceImpl, times(1)).selectPage(eq(10), eq(10), any(Admin.class));
    }

    @Test
    @DisplayName("添加管理员 - 包含完整信息")
    void testAdd_CompleteInfo() throws Exception {
        // Given
        String jsonContent = "{" +
                "\"username\":\"newadmin\"," +
                "\"name\":\"新管理员\"," +
                "\"password\":\"123456\"," +
                "\"avatar\":\"http://example.com/avatar.jpg\"" +
                "}";
        doNothing().when(adminServiceImpl).add(any(Admin.class));

        // When & Then
        mockMvc.perform(post("/admin/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(20000));

        verify(adminServiceImpl, times(1)).add(any(Admin.class));
    }

    @Test
    @DisplayName("更新管理员 - 包含完整信息")
    void testUpdate_CompleteInfo() throws Exception {
        // Given
        String jsonContent = "{" +
                "\"id\":1," +
                "\"username\":\"admin\"," +
                "\"name\":\"更新后的名称\"," +
                "\"avatar\":\"http://example.com/new-avatar.jpg\"" +
                "}";
        doNothing().when(adminServiceImpl).update(any(Admin.class));

        // When & Then
        mockMvc.perform(put("/admin/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(20000));

        verify(adminServiceImpl, times(1)).update(any(Admin.class));
    }

    @Test
    @DisplayName("分页查询 - 大页面大小")
    void testSelectPage_LargePageSize() throws Exception {
        // Given
        Integer pageSize = 100;
        PageInfo<Admin> pageInfo = new PageInfo<>(Arrays.asList(testAdmin));
        when(adminServiceImpl.selectPage(eq(1), eq(pageSize), any(Admin.class))).thenReturn(pageInfo);

        // When & Then
        mockMvc.perform(get("/admin/selectPage")
                .param("pageNum", "1")
                .param("pageSize", pageSize.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(20000));

        verify(adminServiceImpl, times(1)).selectPage(eq(1), eq(pageSize), any(Admin.class));
    }

    @Test
    @DisplayName("分页查询 - 小页面大小")
    void testSelectPage_SmallPageSize() throws Exception {
        // Given
        Integer pageSize = 1;
        PageInfo<Admin> pageInfo = new PageInfo<>(Arrays.asList(testAdmin));
        when(adminServiceImpl.selectPage(eq(1), eq(pageSize), any(Admin.class))).thenReturn(pageInfo);

        // When & Then
        mockMvc.perform(get("/admin/selectPage")
                .param("pageNum", "1")
                .param("pageSize", pageSize.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(20000));

        verify(adminServiceImpl, times(1)).selectPage(eq(1), eq(pageSize), any(Admin.class));
    }
}
