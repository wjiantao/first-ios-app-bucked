package com.shiguang.service;

/**
 * 系统推送服务。
 *
 * <p>在点赞/收藏确为新增并写入站内通知后异步调用：向作品作者注册的 JPush 设备
 * 发送系统通知。未配置推送凭据时降级为仅记日志，不实际发送、不影响业务。</p>
 */
public interface PushService {

    /**
     * 点赞/收藏触发推送。
     *
     * @param recipientId  接收者（作品作者）用户 ID
     * @param actorId      触发者（点赞/收藏的人）用户 ID
     * @param workId       被互动的作品 ID
     * @param workTitle    作品标题（用于推送正文）
     * @param notificationId 站内通知 ID（随 payload 下发，供 App 跳转/幂等）
     * @param type         通知类型：like / favorite
     */
    void onInteraction(String recipientId, String actorId, String workId,
                       String workTitle, long notificationId, String type);
}
