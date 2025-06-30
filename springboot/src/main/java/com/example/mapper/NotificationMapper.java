package com.example.mapper;

import com.example.entity.Notification;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface NotificationMapper {

    @Insert("INSERT INTO notification(user_id, type, title, content, status, created_at) " +
            "VALUES(#{userId}, #{type}, #{title}, #{content}, 'UNREAD', NOW())")
    void insert(Notification notification);

    @Select("SELECT * FROM notification WHERE user_id = #{userId} AND status != 'DELETED' ORDER BY created_at DESC")
    List<Notification> findByUser(@Param("userId") Long userId);

    @Update("UPDATE notification SET status = 'READ', read_at = NOW() WHERE id = #{id}")
    void markAsRead(@Param("id") Long id);

    @Delete("UPDATE notification SET status = 'DELETED' WHERE id = #{id}")
    void deleteLogical(@Param("id") Long id);
}
