package com.example.mapper;

import org.apache.ibatis.annotations.*;

/**
 * UserRoleMapper - 用户角色关联表数据访问层
 */
@Mapper
public interface UserRoleMapper {

    /**
     * 插入用户角色关联
     * @param userId 用户ID
     * @param roleId 角色ID
     */
    @Insert("INSERT INTO user_roles (user_id, role_id) VALUES (#{userId}, #{roleId})")
    void insert(@Param("userId") Integer userId, @Param("roleId") Integer roleId);

    /**
     * 根据角色代码查询角色ID
     * @param roleCode 角色代码（如 SUPER_ADMIN）
     * @return 角色ID
     */
    @Select("SELECT id FROM roles WHERE code = #{roleCode}")
    Integer selectIdByCode(String roleCode);

    /**
     * 删除用户的角色关联
     * @param userId 用户ID
     */
    @Delete("DELETE FROM user_roles WHERE user_id = #{userId}")
    void deleteByUserId(Integer userId);
}
