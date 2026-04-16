package com.example.service;

import com.example.TestBase;
import com.example.entity.Menu;
import com.example.mapper.MenuMapper;
import com.example.service.impl.MenuServiceImpl;
import com.example.utils.RedisUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * MenuServiceImpl 单元测试类
 * 测试菜单相关的业务逻辑
 */
@DisplayName("MenuService 单元测试")
class MenuServiceImplTest extends TestBase {

    @Mock
    private MenuMapper menuMapper;

    @Mock
    private RedisUtils redisUtils;

    @InjectMocks
    private MenuServiceImpl menuService;

    private Menu testMenu;

    @BeforeEach
    void setUp() {
        // 初始化测试数据
        testMenu = new Menu();
        testMenu.setId(1);
        testMenu.setName("首页");
        testMenu.setPath("/home");
        testMenu.setIcon("HomeFilled");
        testMenu.setParentId(0);
        testMenu.setOrderNum(1);
        testMenu.setRole("USER");
    }

    @Test
    @DisplayName("根据角色查询菜单 - 成功（从缓存）")
    void testGetMenuByRole_FromCache() {
        // Given
        String role = "USER";
        List<Menu> cachedMenus = Arrays.asList(testMenu);
        when(redisUtils.get("menu:role:" + role)).thenReturn(cachedMenus);

        // When
        List<Menu> result = menuService.getMenuByRole(role);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(redisUtils, times(1)).get("menu:role:" + role);
        verify(menuMapper, never()).findByRole(anyString());
    }

    @Test
    @DisplayName("根据角色查询菜单 - 成功（从数据库）")
    void testGetMenuByRole_FromDatabase() {
        // Given
        String role = "USER";
        List<Menu> expectedMenus = Arrays.asList(testMenu);
        when(redisUtils.get("menu:role:" + role)).thenReturn(null);
        when(menuMapper.findByRole(role)).thenReturn(expectedMenus);
        when(redisUtils.set(anyString(), anyList(), anyLong(), any())).thenReturn(true);

        // When
        List<Menu> result = menuService.getMenuByRole(role);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(redisUtils, times(1)).get("menu:role:" + role);
        verify(menuMapper, times(1)).findByRole(role);
    }

    @Test
    @DisplayName("查询所有菜单 - 成功（从缓存）")
    void testGetAllMenu_FromCache() {
        // Given
        List<Menu> cachedMenus = Arrays.asList(testMenu);
        when(redisUtils.get("menu:all")).thenReturn(cachedMenus);

        // When
        List<Menu> result = menuService.getAllMenu();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(redisUtils, times(1)).get("menu:all");
        verify(menuMapper, never()).findAll();
    }

    @Test
    @DisplayName("查询所有菜单 - 成功（从数据库）")
    void testGetAllMenu_FromDatabase() {
        // Given
        List<Menu> expectedMenus = Arrays.asList(testMenu);
        when(redisUtils.get("menu:all")).thenReturn(null);
        when(menuMapper.findAll()).thenReturn(expectedMenus);
        when(redisUtils.set(anyString(), anyList(), anyLong(), any())).thenReturn(true);

        // When
        List<Menu> result = menuService.getAllMenu();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(redisUtils, times(1)).get("menu:all");
        verify(menuMapper, times(1)).findAll();
    }

    @Test
    @DisplayName("根据ID查询菜单 - 成功（从缓存）")
    void testGetMenuById_FromCache() {
        // Given
        Integer menuId = 1;
        when(redisUtils.get("menu:id:" + menuId)).thenReturn(testMenu);

        // When
        Menu result = menuService.getMenuById(menuId);

        // Then
        assertNotNull(result);
        assertEquals("首页", result.getName());
        verify(redisUtils, times(1)).get("menu:id:" + menuId);
        verify(menuMapper, never()).findById(anyInt());
    }

    @Test
    @DisplayName("根据ID查询菜单 - 成功（从数据库）")
    void testGetMenuById_FromDatabase() {
        // Given
        Integer menuId = 1;
        when(redisUtils.get("menu:id:" + menuId)).thenReturn(null);
        when(menuMapper.findById(menuId)).thenReturn(testMenu);
        when(redisUtils.set(anyString(), any(Menu.class), anyLong(), any())).thenReturn(true);

        // When
        Menu result = menuService.getMenuById(menuId);

        // Then
        assertNotNull(result);
        assertEquals("首页", result.getName());
        verify(redisUtils, times(1)).get("menu:id:" + menuId);
        verify(menuMapper, times(1)).findById(menuId);
    }

    @Test
    @DisplayName("根据ID查询菜单 - 菜单不存在")
    void testGetMenuById_NotFound() {
        // Given
        Integer menuId = 999;
        when(redisUtils.get("menu:id:" + menuId)).thenReturn(null);
        when(menuMapper.findById(menuId)).thenReturn(null);

        // When
        Menu result = menuService.getMenuById(menuId);

        // Then
        assertNull(result);
        verify(redisUtils, times(1)).get("menu:id:" + menuId);
        verify(menuMapper, times(1)).findById(menuId);
    }

    @Test
    @DisplayName("添加菜单 - 成功")
    void testAddMenu_Success() {
        // Given
        Menu newMenu = new Menu();
        newMenu.setName("用户管理");
        newMenu.setPath("/user");
        newMenu.setIcon("User");
        newMenu.setRole("SUPER_ADMIN");

        // When
        menuService.addMenu(newMenu);

        // Then
        verify(menuMapper, times(1)).insert(newMenu);
    }

    @Test
    @DisplayName("更新菜单 - 成功")
    void testUpdateMenu_Success() {
        // Given
        Menu updateMenu = new Menu();
        updateMenu.setId(1);
        updateMenu.setName("更新后的首页");
        updateMenu.setPath("/dashboard");

        // When
        menuService.updateMenu(updateMenu);

        // Then
        verify(menuMapper, times(1)).update(updateMenu);
        verify(redisUtils, times(1)).remove("menu:id:1");
    }

    @Test
    @DisplayName("删除菜单 - 成功")
    void testDeleteMenu_Success() {
        // Given
        Integer menuId = 1;

        // When
        menuService.deleteMenu(menuId);

        // Then
        verify(menuMapper, times(1)).delete(menuId);
        verify(redisUtils, times(1)).remove("menu:id:1");
    }
}
