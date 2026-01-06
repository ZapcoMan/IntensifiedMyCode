package com.example.controller;

import com.example.common.R;
import com.example.entity.Menu;
import com.example.service.MenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜单控制器，用于处理菜单相关的HTTP请求
 */
@RestController
@RequestMapping("/menu")
public class MenuController {

    @Autowired
    private MenuService menuService;

    /**
     * 根据角色获取菜单列表
     *
     * @param role 用户角色
     * @return 菜单列表
     */
    @GetMapping("/getByRole")
    public R getMenuByRole(@RequestParam String role) {
        List<Menu> menus = menuService.getMenuByRole(role);
        return R.success(menus);
    }

    /**
     * 获取所有菜单
     *
     * @return 所有菜单列表
     */
    @GetMapping("/getAll")
    public R getAllMenu() {
        List<Menu> menus = menuService.getAllMenu();
        return R.success(menus);
    }

    /**
     * 根据ID获取菜单
     *
     * @param id 菜单ID
     * @return 菜单对象
     */
    @GetMapping("/get/{id}")
    public R getMenuById(@PathVariable Integer id) {
        Menu menu = menuService.getMenuById(id);
        return R.success(menu);
    }

    /**
     * 添加菜单
     *
     * @param menu 菜单对象
     * @return 操作结果
     */
    @PostMapping("/add")
    public R addMenu(@RequestBody Menu menu) {
        menuService.addMenu(menu);
        return R.success("添加成功");
    }

    /**
     * 更新菜单
     *
     * @param menu 菜单对象
     * @return 操作结果
     */
    @PutMapping("/update")
    public R updateMenu(@RequestBody Menu menu) {
        menuService.updateMenu(menu);
        return R.success("更新成功");
    }

    /**
     * 删除菜单
     *
     * @param id 菜单ID
     * @return 操作结果
     */
    @DeleteMapping("/delete/{id}")
    public R deleteMenu(@PathVariable Integer id) {
        menuService.deleteMenu(id);
        return R.success("删除成功");
    }
}