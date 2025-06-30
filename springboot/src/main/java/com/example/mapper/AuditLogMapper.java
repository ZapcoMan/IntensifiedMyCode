package com.example.mapper;

import com.example.entity.AuditLog;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AuditLogMapper {
    @Insert("INSERT INTO audit_log(username, action, resource, ip_address, details, created_at) " +
            "VALUES(#{username}, #{action}, #{resource}, #{ipAddress}, #{details}, NOW())")
    void insert(AuditLog log);

    @Select("SELECT * FROM audit_log ORDER BY created_at DESC LIMIT #{limit}")
    List<AuditLog> selectRecent(@Param("limit") int limit);
}
