package com.shiguang.config;

import com.shiguang.push.AuthHandshakeInterceptor;
import com.shiguang.push.NotificationWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * WebSocket 配置。
 *
 * <p>注册站内通知通道 {@code /ws/notifications}，握手时经
 * {@link AuthHandshakeInterceptor} 校验 JWT（URL query token），
 * 连接建立/关闭由 {@link NotificationWebSocketHandler} 维护会话注册表。</p>
 *
 * <p>打开跨域是为了便于 Web 端调试；RN 原生 WebSocket 不受浏览器同源限制。</p>
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Autowired
    private AuthHandshakeInterceptor authHandshakeInterceptor;

    @Autowired
    private NotificationWebSocketHandler notificationWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(notificationWebSocketHandler, "/ws/notifications")
                .addInterceptors(authHandshakeInterceptor)
                .setAllowedOrigins("*");
    }
}
