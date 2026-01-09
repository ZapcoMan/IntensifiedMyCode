package com.example.mapper;

import com.example.entity.Notification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 通知映射器接口，用于定义对通知数据进行CRUD操作的方法
 * 使用MyBatis的@Mapper注解标识这是一个MyBatis映射器接口
 */
@Mapper
public interface NotificationMapper {

    /**
     * 插入一条新的通知记录到数据库中
     *
     * @param notification 要插入的通知对象，包含用户ID、通知类型、标题、内容等信息
     * 注释解释了为什么使用NOW()函数：为了自动记录通知的创建时间
     */
    void insert(Notification notification);

    /**
     * 根据用户ID查找该用户的所有未删除的通知，并按照创建时间降序排列
     *
     * @param userId 用户ID，用于查询属于该用户的通知
     * @return 返回一个通知列表，包含所有符合条件的通知对象
     * 注释解释了查询条件和排序方式：为了确保只获取未删除的通知，并且最新创建的通知排在前面
     */
    List<Notification> findByUser(@Param("userId") Long userId);

    /**
     * 将指定通知标记为已读
     *
     * @param id 通知的ID，用于标识要标记为已读的通知
     * 注释解释了更新操作的目的：记录通知的阅读状态和阅读时间
     */
    void markAsRead(@Param("id") Long id);

    /**
     * 逻辑删除通知记录，即在数据库中标记通知为已删除状态，而不是物理删除
     *
     * @param id 通知的ID，用于标识要逻辑删除的通知
     * 注释解释了为什么使用逻辑删除而非物理删除：可能是为了保留数据，以便未来查询或审计
     */
    void deleteLogical(@Param("id") Long id);
    
    /**
     * 根据ID查询通知
     *
     * @param id 通知的ID
     * @return 通知对象
     */
    Notification findById(@Param("id") Long id);
}