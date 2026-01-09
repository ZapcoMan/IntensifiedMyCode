package com.example.service.impl;

import com.example.entity.Menu;
import com.example.mapper.MenuMapper;
import com.example.service.MenuService;
import com.example.utils.RedisUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MenuServiceImpl implements MenuService {

    @Autowired
    private MenuMapper menuMapper;
    
    @Autowired
    private RedisUtils redisUtils;

    @Override
    public List<Menu> getMenuByRole(String role) {
        // 先从Redis获取缓存的菜单数据
        String cacheKey = "menu:role:" + role;
        List<Menu> cachedMenus = redisUtils.get(cacheKey);
        
        if (cachedMenus != null && !cachedMenus.isEmpty()) {
            return cachedMenus;
        }
        
        // 如果Redis中没有，则从数据库查询
        List<Menu> menus = menuMapper.findByRole(role);
        List<Menu> menuTree = buildMenuTree(menus);
        
        // 将结果缓存到Redis，设置有效期为1小时
        redisUtils.set(cacheKey, menuTree, 60, java.util.concurrent.TimeUnit.MINUTES);
        
        return menuTree;
    }

    @Override
    public List<Menu> getAllMenu() {
        // 先从Redis获取缓存的菜单数据
        String cacheKey = "menu:all";
        List<Menu> cachedMenus = redisUtils.get(cacheKey);
        
        if (cachedMenus != null && !cachedMenus.isEmpty()) {
            return cachedMenus;
        }
        
        // 如果Redis中没有，则从数据库查询
        List<Menu> allMenus = menuMapper.findAll();
        List<Menu> menuTree = buildMenuTree(allMenus);
        
        // 将结果缓存到Redis，设置有效期为1小时
        redisUtils.set(cacheKey, menuTree, 60, java.util.concurrent.TimeUnit.MINUTES);
        
        return menuTree;
    }

    @Override
    public Menu getMenuById(Integer id) {
        // 先从Redis获取缓存的菜单数据
        String cacheKey = "menu:id:" + id;
        Menu cachedMenu = redisUtils.get(cacheKey);
        
        if (cachedMenu != null) {
            return cachedMenu;
        }
        
        // 如果Redis中没有，则从数据库查询
        Menu menu = menuMapper.findById(id);
        
        // 将结果缓存到Redis，设置有效期为1小时
        if (menu != null) {
            redisUtils.set(cacheKey, menu, 60, java.util.concurrent.TimeUnit.MINUTES);
        }
        
        return menu;
    }

    @Override
    public void addMenu(Menu menu) {
        menuMapper.insert(menu);
        
        // 添加菜单后，清除相关的缓存
        clearMenuCache();
    }

    @Override
    public void updateMenu(Menu menu) {
        menuMapper.update(menu);
        
        // 更新菜单后，清除相关的缓存
        String cacheKey = "menu:id:" + menu.getId();
        redisUtils.remove(cacheKey);
        clearMenuCache();
    }

    @Override
    public void deleteMenu(Integer id) {
        menuMapper.delete(id);
        
        // 删除菜单后，清除相关的缓存
        String cacheKey = "menu:id:" + id;
        redisUtils.remove(cacheKey);
        clearMenuCache();
    }

    /**
     * 清除菜单相关的缓存
     */
    private void clearMenuCache() {
        // 获取所有可能的缓存键并清除
        redisUtils.remove("menu:all");
        
        // 这里可以清除所有角色相关的菜单缓存，或使用通配符（如果Redis支持）
        // 为了简单起见，可以清除所有菜单缓存
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