package com.example.enums;

import lombok.Getter;

/**
 * 角色枚举类，统一定义系统中所有角色
 * 所有角色相关的判断都应使用此枚举，避免硬编码字符串
 */
@Getter
public enum RoleEnum {
    SUPER_ADMIN("SUPER_ADMIN", "超级管理员"),
    DEPT_ADMIN("DEPT_ADMIN", "部门管理员"),
    CLUB_LEADER("CLUB_LEADER", "社团负责人"),
    USER("USER", "普通用户");

    private final String code;
    private final String label;

    RoleEnum(String code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 根据角色代码获取枚举
     * @param code 角色代码
     * @return 对应的RoleEnum枚举值
     * @throws IllegalArgumentException 如果角色代码无效
     */
    public static RoleEnum fromCode(String code) {
        for (RoleEnum value : RoleEnum.values()) {
            if (value.getCode().equalsIgnoreCase(code)) {
                return value;
            }
        }
        throw new IllegalArgumentException("无效角色标识: " + code);
    }

    /**
     * 判断是否为管理员角色
     * @param code 角色代码
     * @return true 如果是管理员角色
     */
    public static boolean isAdminRole(String code) {
        return SUPER_ADMIN.code.equals(code) 
            || DEPT_ADMIN.code.equals(code) 
            || CLUB_LEADER.code.equals(code);
    }

    /**
     * 判断是否为普通用户角色
     * @param code 角色代码
     * @return true 如果是普通用户角色
     */
    public static boolean isUserRole(String code) {
        return USER.code.equals(code);
    }
}