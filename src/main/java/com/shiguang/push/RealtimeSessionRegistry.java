package com.shiguang.push;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket 会话注册表。
 *
 * <p>以 userId 为键维护该用户当前所有已认证的 WebSocket 会话，
 * 支持同一用户多端（多设备/多连接）。连接建立时加入、关闭时移除，
 * 空集合即时清理，避免长期占用内存。</p>
 */
@Component
public class RealtimeSessionRegistry {

    private final Map<String, Set<WebSocketSession>> sessions = new ConcurrentHashMap<>();

    /** 为某用户登记一个连接。 */
    public void add(String userId, WebSocketSession session) {
        sessions.computeIfAbsent(userId, key -> ConcurrentHashMap.newKeySet()).add(session);
    }

    /** 移除某用户的一个连接；空集合并清理该用户的键。 */
    public void remove(String userId, WebSocketSession session) {
        Set<WebSocketSession> set = sessions.get(userId);
        if (set == null) {
            return;
        }
        set.remove(session);
        if (set.isEmpty()) {
            sessions.remove(userId);
        }
    }

    /** 取某用户当前全部连接；无连接时返回空集合，避免调用方判空。 */
    public Set<WebSocketSession> get(String userId) {
        Set<WebSocketSession> set = sessions.get(userId);
        return set == null ? Collections.emptySet() : set;
    }
}
