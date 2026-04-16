package com.example.service;

import com.example.entity.Menu;
import com.example.mapper.MenuMapper;
import com.example.service.impl.MenuServiceImpl;
import com.example.utils.RedisUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * MenuServiceImpl 单元测试
 * 重点测试：
 * 1. 菜单树形结构构建（buildMenuTree）
 * 2. Redis 缓存命中 / 未命中逻辑
 * 3. 缓存清除策略
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MenuServiceTest {

    @Autowired
    private MenuServiceImpl menuService;

    @MockBean
    private RedisUtils redisUtils;

    @BeforeEach
    void setUp() {
        when(redisUtils.get(anyString())).thenReturn(null);
        when(redisUtils.set(anyString(), any(), anyLong(), any())).thenReturn(true);
        when(redisUtils.remove(anyString())).thenReturn(true);
    }

    // ========== 树形结构构建测试 ==========

    @Test
    void getMenuByRole_构建正确的树形结构() {
        // given: 数据库有父子菜单数据
        // V1 迁移脚本插入了示例数据：首页(parentId=0)、用户管理(parentId=0)、
        // 超级管理员信息(parentId=2)、用户信息(parentId=2)
        when(menuMapper.findByRole("SUPER_ADMIN")).thenReturn(Arrays.asList(
                createMenu(1, "首页", "/manager/index", 0, "House", 1),
                createMenu(2, "用户管理", "", 0, "User", 2),
                createMenu(3, "超级管理员信息", "/manager/admin", 2, "", 1),
                createMenu(4, "用户信息", "/manager/user", 2, "", 2)
        ));

        // when
        List<Menu> tree = menuService.getMenuByRole("SUPER_ADMIN");

        // then: 根菜单只有 2 个
        assertEquals(2, tree.size());
        // 首页是第一个根菜单
        Menu indexMenu = tree.get(0);
        assertEquals("首页", indexMenu.getName());
        assertNull(indexMenu.getChildren()); // 首页无子菜单

        // 用户管理是第二个根菜单，有子菜单
        Menu userMgmt = tree.get(1);
        assertEquals("用户管理", userMgmt.getName());
        assertNotNull(userMgmt.getChildren());
        assertEquals(2, userMgmt.getChildren().size());
        assertEquals("超级管理员信息", userMgmt.getChildren().get(0).getName());
    }

    @Test
    void getMenuByRole_按排序号升序排列() {
        // given: 乱序的菜单数据
        when(menuMapper.findByRole("USER")).thenReturn(Arrays.asList(
                createMenu(3, "第三项", "/p3", 0, "", 3),
                createMenu(1, "第一项", "/p1", 0, "", 1),
                createMenu(2, "第二项", "/p2", 0, "", 2)
        ));

        // when
        List<Menu> tree = menuService.getMenuByRole("USER");

        // then: 按 orderNum 排序
        assertEquals("第一项", tree.get(0).getName());
        assertEquals("第二项", tree.get(1).getName());
        assertEquals("第三项", tree.get(2).getName());
    }

    @Test
    void getMenuByRole_三级嵌套菜单() {
        // given: 三级嵌套菜单
        when(menuMapper.findByRole("SUPER_ADMIN")).thenReturn(Arrays.asList(
                createMenu(1, "一级菜单", "/level1", 0, "", 1),
                createMenu(2, "二级菜单", "/level2", 1, "", 1),
                createMenu(3, "三级菜单", "/level3", 2, "", 1)
        ));

        // when
        List<Menu> tree = menuService.getMenuByRole("SUPER_ADMIN");

        // then
        assertEquals(1, tree.size());
        Menu level1 = tree.get(0);
        assertEquals(1, level1.getChildren().size());
        assertEquals("二级菜单", level1.getChildren().get(0).getName());
        assertEquals(1, level1.getChildren().get(0).getChildren().size());
        assertEquals("三级菜单", level1.getChildren().get(0).getChildren().get(0).getName());
    }

    // ========== 缓存测试 ==========

    @Test
    void getMenuByRole_缓存命中_不查数据库() {
        // given: Redis 有缓存
        Menu cachedMenu = createMenu(99, "缓存菜单", "/cached", 0, "", 1);
        when(redisUtils.get("menu:role:USER")).thenReturn(Collections.singletonList(cachedMenu));

        // when
        List<Menu> result = menuService.getMenuByRole("USER");

        // then
        assertEquals(1, result.size());
        assertEquals("缓存菜单", result.get(0).getName());
        verify(menuMapper, never()).findByRole(anyString());
    }

    @Test
    void getMenuByRole_缓存未命中_查数据库并缓存结果() {
        // given
        when(redisUtils.get("menu:role:SUPER_ADMIN")).thenReturn(null);
        when(menuMapper.findByRole("SUPER_ADMIN")).thenReturn(Collections.emptyList());

        // when
        List<Menu> result = menuService.getMenuByRole("SUPER_ADMIN");

        // then
        verify(menuMapper).findByRole("SUPER_ADMIN");
        verify(redisUtils).set(eq("menu:role:SUPER_ADMIN"), any(), eq(60L), any());
    }

    @Test
    void getAllMenu_缓存命中() {
        // given
        Menu menu = createMenu(1, "全部菜单", "/all", 0, "", 1);
        when(redisUtils.get("menu:all")).thenReturn(Collections.singletonList(menu));

        // when
        List<Menu> result = menuService.getAllMenu();

        // then
        assertEquals("全部菜单", result.get(0).getName());
        verify(menuMapper, never()).findAll();
    }

    @Test
    void getMenuById_缓存命中() {
        // given
        Menu cached = createMenu(5, "ID菜单", "/id5", 0, "", 1);
        when(redisUtils.get("menu:id:5")).thenReturn(cached);

        // when
        Menu result = menuService.getMenuById(5);

        // then
        assertEquals("ID菜单", result.getName());
        verify(menuMapper, never()).findById(anyInt());
    }

    // ========== 写操作测试 ==========

    @Test
    void addMenu_插入并清除全量缓存() {
        // given
        Menu newMenu = createMenu(null, "新菜单", "/new", 0, "Plus", 1);

        // when
        menuService.addMenu(newMenu);

        // then: 验证全量缓存被清除
        verify(redisUtils).remove("menu:all");
    }

    @Test
    void updateMenu_更新并清除对应缓存() {
        // given: 已有菜单
        menuService.addMenu(createMenu(null, "待更新", "/toUpdate", 0, "", 1));
        when(menuMapper.findById(anyInt())).thenReturn(createMenu(10, "待更新", "/toUpdate", 0, "", 1));

        // when
        menuService.updateMenu(createMenu(10, "已更新", "/updated", 0, "", 1));

        // then: 验证特定缓存和全量缓存都被清除
        verify(redisUtils).remove("menu:id:10");
        verify(redisUtils).remove("menu:all");
    }

    @Test
    void deleteMenu_删除并清除缓存() {
        // given: 已有菜单
        menuService.addMenu(createMenu(null, "待删除", "/toDelete", 0, "", 1));
        when(menuMapper.findById(anyInt())).thenReturn(createMenu(10, "待删除", "/toDelete", 0, "", 1));

        // when
        menuService.deleteMenu(10);

        // then
        verify(redisUtils).remove("menu:id:10");
        verify(redisUtils).remove("menu:all");
    }

    // ========== 边界测试 ==========

    @Test
    void getMenuByRole_数据库无数据_返回空列表() {
        // given
        when(menuMapper.findByRole("EMPTY_ROLE")).thenReturn(Collections.emptyList());

        // when
        List<Menu> result = menuService.getMenuByRole("EMPTY_ROLE");

        // then
        assertTrue(result.isEmpty());
    }

    @Test
    void getMenuById_数据库无数据_返回null() {
        // given
        when(menuMapper.findById(9999)).thenReturn(null);

        // when
        Menu result = menuService.getMenuById(9999);

        // then
        assertNull(result);
    }

    // ========== 辅助方法 ==========
    private Menu createMenu(Integer id, String name, String path, Integer parentId, String icon, Integer orderNum) {
        Menu menu = new Menu();
        menu.setId(id);
        menu.setName(name);
        menu.setPath(path);
        menu.setParentId(parentId);
        menu.setIcon(icon);
        menu.setOrderNum(orderNum);
        menu.setRole("USER");
        menu.setStatus("ENABLE");
        return menu;
    }
}
