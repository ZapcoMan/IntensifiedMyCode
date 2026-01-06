package com.example.mapper;

import com.example.entity.Menu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 菜单映射器接口，用于定义对菜单数据进行CRUD操作的方法
 * 使用MyBatis的@Mapper注解标识这是一个MyBatis映射器接口
 */
@Mapper
public interface MenuMapper {

    /**
     * 根据角色查询菜单列表
     *
     * @param role 用户角色
     * @return 菜单列表
     */
    List<Menu> findByRole(@Param("role") String role);

    /**
     * 查询所有菜单
     *
     * @return 所有菜单列表
     */
    List<Menu> findAll();

    /**
     * 根据ID查询菜单
     *
     * @param id 菜单ID
     * @return 菜单对象
     */
    Menu findById(@Param("id") Integer id);

    /**
     * 插入新菜单
     *
     * @param menu 菜单对象
     */
    void insert(Menu menu);

    /**
     * 更新菜单
     *
     * @param menu 菜单对象
     */
    void update(Menu menu);

    /**
     * 删除菜单
     *
     * @param id 菜单ID
     */
    void delete(@Param("id") Integer id);
}