package com.example.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

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
    void insert(@Param("userId") Integer userId, @Param("roleId") Integer roleId);

    /**
     * 根据角色代码查询角色ID
     * @param roleCode 角色代码（如 SUPER_ADMIN）
     * @return 角色ID
     */
    Integer selectIdByCode(String roleCode);

    /**
     * 删除用户的角色关联
     * @param userId 用户ID
     */
    void deleteByUserId(Integer userId);
}
