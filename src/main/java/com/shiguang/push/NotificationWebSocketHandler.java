package com.shiguang.push;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/**
 * 站内通知 WebSocket 处理器。
 *
 * <p>只接收连接建立/关闭事件，把会话登记到 {@link RealtimeSessionRegistry}；
 * 下行消息由 {@link RealtimePushService} 统一推送，这里不处理客户端上行数据。</p>
 */
@Component
@Slf4j
public class NotificationWebSocketHandler extends TextWebSocketHandler {

    /** 存放在 session attributes 中、标识登录用户 ID 的键。 */
    public static final String ATTR_USER_ID = "userId";

    @Autowired
    private RealtimeSessionRegistry registry;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Object userId = session.getAttributes().get(ATTR_USER_ID);
        if (userId != null) {
            registry.add(userId.toString(), session);
            log.debug("WebSocket 连接建立 userId={}", userId);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Object userId = session.getAttributes().get(ATTR_USER_ID);
        if (userId != null) {
            registry.remove(userId.toString(), session);
            log.debug("WebSocket 连接关闭 userId={}", userId);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        // 客户端当前不需要上行消息，这里留空。
    }
}
