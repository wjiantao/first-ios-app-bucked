package com.shiguang.service;

/**
 * 站内通知 WebSocket 推送服务。
 *
 * <p>在点赞/收藏确为新增并写入站内通知后调用：向作品作者当前所有已连接的
 * WebSocket 会话推送一条 {@code {type:'notification', data:{...}}} 消息，
 * 使客户端实时刷新未读角标与消息列表。与 JPush 系统推送并存、互不替代。</p>
 */
public interface RealtimePushService {

    /**
     * 向接收者的全部实时连接推送一条站内通知。
     *
     * @param recipientId   接收者（作品作者）用户 ID
     * @param actorId       触发者（点赞/收藏的人）用户 ID
     * @param workId        被互动的作品 ID
     * @param workTitle     作品标题
     * @param notificationId 站内通知 ID
     * @param type          通知类型：like / favorite
     */
    void push(String recipientId, String actorId, String workId,
              String workTitle, long notificationId, String type);
}
