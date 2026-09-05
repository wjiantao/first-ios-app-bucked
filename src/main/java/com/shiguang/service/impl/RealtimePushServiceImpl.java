package com.shiguang.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shiguang.entity.User;
import com.shiguang.mapper.NotificationMapper;
import com.shiguang.mapper.UserMapper;
import com.shiguang.push.RealtimeSessionRegistry;
import com.shiguang.service.RealtimePushService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * 站内通知 WebSocket 推送服务实现。
 *
 * <p>pushes 到接收者所有已认证会话；用 {@link ConcurrentWebSocketSessionDecorator}
 * 做线程安全发送，避免多个请求线程同时写同一 session 抛异常。
 * 会话已关闭时移除并继续，保证单条失败不影响其它连接。</p>
 */
@Service
@Slf4j
public class RealtimePushServiceImpl implements RealtimePushService {

    /** 单条发送超时与缓冲区上限：超时视为该会话异常，移除后继续。 */
    private static final int SEND_TIME_LIMIT_MS = 5000;
    private static final int BUFFER_SIZE_LIMIT = 65_536;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired
    private RealtimeSessionRegistry registry;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private NotificationMapper notificationMapper;

    @Override
    public void push(String recipientId, String actorId, String workId,
                     String workTitle, long notificationId, String type) {
        Set<WebSocketSession> sessions = registry.get(recipientId);
        if (sessions.isEmpty()) {
            return;
        }

        User actor = userMapper.getById(actorId);
        String nickname = (actor != null && actor.getNickname() != null && !actor.getNickname().isBlank())
                ? actor.getNickname() : "用户";

        // data 字段与客户端约定保持一致：notifType/notificationId/workId/
        // actorId/actorNickname/workTitle/createdAt/unreadCount。
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("notifType", type);
        data.put("notificationId", notificationId);
        data.put("workId", workId);
        data.put("actorId", actorId);
        data.put("actorNickname", nickname);
        data.put("workTitle", workTitle != null ? workTitle : "");
        data.put("createdAt", LocalDateTime.now().toString());
        data.put("unreadCount", notificationMapper.countUnread(recipientId));

        Map<String, Object> root = new LinkedHashMap<>();
        root.put("type", "notification");
        root.put("data", data);

        String json;
        try {
            json = OBJECT_MAPPER.writeValueAsString(root);
        } catch (JsonProcessingException ex) {
            log.error("序列化实时通知失败 recipientId={} type={}", recipientId, type, ex);
            return;
        }

        for (WebSocketSession session : sessions) {
            try {
                ConcurrentWebSocketSessionDecorator decorator =
                        new ConcurrentWebSocketSessionDecorator(session, SEND_TIME_LIMIT_MS, BUFFER_SIZE_LIMIT);
                decorator.sendMessage(new TextMessage(json));
                log.debug("WebSocket 推送成功 userId={} type={}", recipientId, type);
            } catch (Exception ex) {
                log.warn("WebSocket 推送失败，移除会话 userId={}", recipientId, ex);
                registry.remove(recipientId, session);
            }
        }
    }
}
