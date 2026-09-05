package com.shiguang.mapper;

import com.github.pagehelper.Page;
import com.shiguang.entity.Notification;
import com.shiguang.vo.NotificationVO;
import org.apache.ibatis.annotations.Param;

/**
 * 通知表数据访问接口。
 *
 * 与 UserMapper 一致：SQL 收敛在 resources/mapper/NotificationMapper.xml 中。
 * pageByRecipient 需配合 PageHelper.startPage 使用，返回即分页后的 Page&lt;NotificationVO&gt;。
 */
public interface NotificationMapper {

    /** 插入一条通知，返回影响行数。 */
    int insert(Notification notification);

    /** 接收者通知分页（倒序），配合 PageHelper.startPage 使用。 */
    Page<NotificationVO> pageByRecipient(@Param("recipientId") String recipientId);

    /** 统计接收者未读通知数。 */
    long countUnread(@Param("recipientId") String recipientId);

    /** 把接收者所有未读通知置为已读，返回影响行数。 */
    int markAllRead(@Param("recipientId") String recipientId);

    /** 把指定（且属于该接收者）的通知置为已读，返回影响行数。 */
    int markRead(@Param("id") Long id, @Param("recipientId") String recipientId);
}
