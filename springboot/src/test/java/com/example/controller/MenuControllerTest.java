package com.example.controller;

import com.example.TestBase;
import com.example.entity.Menu;
import com.example.service.MenuService;
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
 * MenuController 单元测试类
 * 测试菜单相关的REST API接口
 */
@DisplayName("MenuController 单元测试")
class MenuControllerTest extends TestBase {

    @Mock
    private MenuService menuService;

    @InjectMocks
    private MenuController menuController;

    private MockMvc mockMvc;
    private Menu testMenu;

    @BeforeEach
    void setUp() {
        // 初始化MockMvc
        mockMvc = MockMvcBuilders.standaloneSetup(menuController).build();

        // 初始化测试数据
        testMenu = new Menu();
        testMenu.setId(1);
        testMenu.setName("首页");
        testMenu.setPath("/home");
        testMenu.setIcon("HomeFilled");
        testMenu.setRole("USER");
    }

    @Test
    @DisplayName("根据角色获取菜单 - 成功")
    void testGetMenuByRole_Success() throws Exception {
        // Given
        String role = "USER";
        List<Menu> menus = Arrays.asList(testMenu);
        when(menuService.getMenuByRole(role)).thenReturn(menus);

        // When & Then
        mockMvc.perform(get("/menu/getByRole")
                .param("role", role))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(20000));

        verify(menuService, times(1)).getMenuByRole(role);
    }

    @Test
    @DisplayName("获取所有菜单 - 成功")
    void testGetAllMenu_Success() throws Exception {
        // Given
        List<Menu> menus = Arrays.asList(testMenu);
        when(menuService.getAllMenu()).thenReturn(menus);

        // When & Then
        mockMvc.perform(get("/menu/getAll"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(20000));

        verify(menuService, times(1)).getAllMenu();
    }

    @Test
    @DisplayName("根据ID获取菜单 - 成功")
    void testGetMenuById_Success() throws Exception {
        // Given
        Integer menuId = 1;
        when(menuService.getMenuById(menuId)).thenReturn(testMenu);

        // When & Then
        mockMvc.perform(get("/menu/get/{id}", menuId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(20000));

        verify(menuService, times(1)).getMenuById(menuId);
    }

    @Test
    @DisplayName("添加菜单 - 成功")
    void testAddMenu_Success() throws Exception {
        // Given
        doNothing().when(menuService).addMenu(any(Menu.class));

        // When & Then
        mockMvc.perform(post("/menu/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"用户管理\",\"path\":\"/user\",\"icon\":\"User\",\"role\":\"SUPER_ADMIN\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(20000));

        verify(menuService, times(1)).addMenu(any(Menu.class));
    }

    @Test
    @DisplayName("更新菜单 - 成功")
    void testUpdateMenu_Success() throws Exception {
        // Given
        doNothing().when(menuService).updateMenu(any(Menu.class));

        // When & Then
        mockMvc.perform(put("/menu/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":1,\"name\":\"更新后的首页\",\"path\":\"/dashboard\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(20000));

        verify(menuService, times(1)).updateMenu(any(Menu.class));
    }

    @Test
    @DisplayName("删除菜单 - 成功")
    void testDeleteMenu_Success() throws Exception {
        // Given
        Integer menuId = 1;
        doNothing().when(menuService).deleteMenu(menuId);

        // When & Then
        mockMvc.perform(delete("/menu/delete/{id}", menuId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(20000));

        verify(menuService, times(1)).deleteMenu(menuId);
    }
}
