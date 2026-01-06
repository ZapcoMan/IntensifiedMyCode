package com.example.service.impl;

import com.example.entity.Menu;
import com.example.mapper.MenuMapper;
import com.example.service.MenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 菜单服务实现类
 */
@Service
public class MenuServiceImpl implements MenuService {

    @Autowired
    private MenuMapper menuMapper;

    @Override
    public List<Menu> getMenuByRole(String role) {
        // 首先查询指定角色的菜单
        List<Menu> menus = menuMapper.findByRole(role);
        
        // 构建菜单树形结构
        return buildMenuTree(menus);
    }

    @Override
    public List<Menu> getAllMenu() {
        List<Menu> allMenus = menuMapper.findAll();
        return buildMenuTree(allMenus);
    }

    @Override
    public Menu getMenuById(Integer id) {
        return menuMapper.findById(id);
    }

    @Override
    public void addMenu(Menu menu) {
        menuMapper.insert(menu);
    }

    @Override
    public void updateMenu(Menu menu) {
        menuMapper.update(menu);
    }

    @Override
    public void deleteMenu(Integer id) {
        menuMapper.delete(id);
    }

    /**
     * 构建菜单树形结构
     *
     * @param menus 菜单列表
     * @return 树形结构的菜单列表
     */
    private List<Menu> buildMenuTree(List<Menu> menus) {
        // 找出所有父级菜单（parentId为0的菜单）
        List<Menu> parentMenus = menus.stream()
                .filter(menu -> menu.getParentId() == 0)
                .sorted((m1, m2) -> Integer.compare(m1.getOrderNum(), m2.getOrderNum()))
                .collect(Collectors.toList());

        // 为每个父级菜单设置子菜单
        for (Menu parentMenu : parentMenus) {
            List<Menu> children = getChildMenus(parentMenu.getId(), menus);
            parentMenu.setChildren(children);
        }

        return parentMenus;
    }

    /**
     * 递归获取子菜单
     *
     * @param parentId 父菜单ID
     * @param menus    所有菜单列表
     * @return 子菜单列表
     */
    private List<Menu> getChildMenus(Integer parentId, List<Menu> menus) {
        List<Menu> children = menus.stream()
                .filter(menu -> menu.getParentId().equals(parentId))
                .sorted((m1, m2) -> Integer.compare(m1.getOrderNum(), m2.getOrderNum()))
                .collect(Collectors.toList());

        for (Menu child : children) {
            List<Menu> subChildren = getChildMenus(child.getId(), menus);
            child.setChildren(subChildren);
        }

        return children;
    }
}