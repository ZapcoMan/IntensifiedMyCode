package com.example.service;

import com.example.entity.Menu;

import java.util.List;

/**
 * MenuService接口定义了菜单服务的通用操作，包括查询菜单列表、获取菜单详情等
 */
public interface MenuService {

    /**
     * 根据角色查询菜单列表
     *
     * @param role 用户角色
     * @return 菜单列表
     */
    List<Menu> getMenuByRole(String role);

    /**
     * 查询所有菜单
     *
     * @return 所有菜单列表
     */
    List<Menu> getAllMenu();

    /**
     * 根据ID查询菜单
     *
     * @param id 菜单ID
     * @return 菜单对象
     */
    Menu getMenuById(Integer id);

    /**
     * 添加菜单
     *
     * @param menu 菜单对象
     */
    void addMenu(Menu menu);

    /**
     * 更新菜单
     *
     * @param menu 菜单对象
     */
    void updateMenu(Menu menu);

    /**
     * 删除菜单
     *
     * @param id 菜单ID
     */
    void deleteMenu(Integer id);
}