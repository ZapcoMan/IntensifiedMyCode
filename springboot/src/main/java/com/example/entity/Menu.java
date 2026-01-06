package com.example.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Menu类代表系统中的菜单信息
 * 它封装了菜单相关的各种属性，如名称、路径、图标等
 * 该类使用了Lombok库的注解，以自动生成setter和getter方法，简化代码
 */
@NoArgsConstructor  // 生成无参构造函数
@AllArgsConstructor // 生成全参构造函数
@Data
public class Menu {

    /**
     * 菜单ID，唯一标识一个菜单项
     */
    private Integer id;

    /**
     * 菜单名称，用于在界面显示菜单项的名称
     */
    private String name;

    /**
     * 菜单路径，用于路由跳转
     */
    private String path;

    /**
     * 父菜单ID，用于构建菜单层级结构
     */
    private Integer parentId;

    /**
     * 菜单图标，用于在界面显示菜单项的图标
     */
    private String icon;

    /**
     * 排序号，用于确定菜单项的显示顺序
     */
    private Integer orderNum;

    /**
     * 角色权限，决定了哪些角色可以访问该菜单
     */
    private String role;

    /**
     * 组件路径，指定菜单对应的页面组件
     */
    private String component;

    /**
     * 状态，表示菜单是否启用
     */
    private String status;

    /**
     * 子菜单列表，用于构建菜单层级结构
     */
    private List<Menu> children;
}