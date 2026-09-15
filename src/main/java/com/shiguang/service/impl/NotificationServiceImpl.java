package com.shiguang.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.shiguang.context.UserContext;
import com.shiguang.dto.ProfilePageQueryDTO;
import com.shiguang.entity.Notification;
import com.shiguang.mapper.NotificationMapper;
import com.shiguang.result.PageResult;
import com.shiguang.service.NotificationService;
import com.shiguang.vo.NotificationVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 站内通知服务实现。
 *
 * 接收者一律取当前登录用户；消息中心列表走 PageHelper 分页，
 * 已读操作按 recipient 隔离，防止用户修改他人通知。
 */
@Service
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationMapper notificationMapper;

    @Override
    public long create(String recipientId, String actorId, String workId, String type) {
        return create(recipientId, actorId, workId, type, null, null);
    }

    @Override
    public long create(String recipientId, String actorId, String workId, String type, Long commentId, String commentContent) {
        Notification notification = new Notification();
        notification.setRecipientId(recipientId);
        notification.setActorId(actorId);
        notification.setWorkId(workId);
        notification.setType(type);
        notification.setCommentId(commentId);
        notification.setCommentContent(commentContent);
        notification.setCreatedAt(LocalDateTime.now());
        notificationMapper.insert(notification);
        log.info("写入通知成功 recipientId={} actorId={} workId={} type={} id={}",
                recipientId, actorId, workId, type, notification.getId());
        return notification.getId();
    }

    @Override
    public PageResult<NotificationVO> page(ProfilePageQueryDTO profilePageQueryDTO) {
        String recipientId = UserContext.getCurrentId();
        PageHelper.startPage(profilePageQueryDTO.getCurrent(), profilePageQueryDTO.getPageSize());
        Page<NotificationVO> page = notificationMapper.pageByRecipient(recipientId);
        return new PageResult<>(page.getTotal(), page.getResult());
    }

    @Override
    public long unreadCount() {
        return notificationMapper.countUnread(UserContext.getCurrentId());
    }

    @Override
    public int markAllRead() {
        return notificationMapper.markAllRead(UserContext.getCurrentId());
    }

    @Override
    public int markRead(Long id) {
        return notificationMapper.markRead(id, UserContext.getCurrentId());
    }
}
