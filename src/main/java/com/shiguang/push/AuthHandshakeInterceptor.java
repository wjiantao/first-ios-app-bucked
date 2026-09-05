package com.shiguang.push;

import com.shiguang.constant.JwtClaimsConstant;
import com.shiguang.properties.JwtProperties;
import com.shiguang.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * WebSocket 握手鉴权拦截器。
 *
 * <p>RN 原生 WebSocket 跨平台设置请求头不可靠，因此 JWT 通过 URL query
 * {@code ?token=<jwt>} 传递；这里解析并校验，通过后把 userId 写入会话属性，
 * 供 {@link NotificationWebSocketHandler} 在连接建立时注册。</p>
 *
 * <p>该路径由 spring-websocket 的 handler mapping 处理，不走 DispatcherServlet，
 * 因此不会被 HTTP 层的 {@code JwtTokenUserInterceptor} 拦截，二者互不冲突。</p>
 */
@Component
@Slf4j
public class AuthHandshakeInterceptor implements HandshakeInterceptor {

    @Autowired
    private JwtProperties jwtProperties;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) {
        String token = extractToken(request.getURI());
        if (token == null || token.isBlank()) {
            return false;
        }
        try {
            Claims claims = JwtUtil.parseJWT(jwtProperties.getSecretKey(), token);
            Object userId = claims.get(JwtClaimsConstant.USER_ID);
            if (userId == null) {
                return false;
            }
            attributes.put(NotificationWebSocketHandler.ATTR_USER_ID, userId.toString());
            return true;
        } catch (Exception ex) {
            log.warn("WebSocket 握手鉴权失败: {}", ex.getMessage());
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request,
                               ServerHttpResponse response,
                               WebSocketHandler wsHandler,
                               Exception exception) {
        // 握手后无需额外处理。
    }

    /** 从 URL query 中提取 token（首个匹配），未携带返回 null。 */
    private String extractToken(URI uri) {
        String query = uri.getRawQuery();
        if (query == null) {
            return null;
        }
        for (String pair : query.split("&")) {
            int index = pair.indexOf('=');
            if (index > 0 && "token".equals(pair.substring(0, index))) {
                return URLDecoder.decode(pair.substring(index + 1), StandardCharsets.UTF_8);
            }
        }
        return null;
    }
}
