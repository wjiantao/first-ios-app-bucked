package com.shiguang.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 通知实体，对应 notifications 表。
 *
 * 当他人点赞/收藏某篇「已发布」作品或关注用户时，给接收者写入一条通知；
 * 取消关系不删除历史通知。关注通知不关联作品，因此 workId 可以为空。
 */
@Data
public class Notification {

    private Long id;

    /** 接收者用户 ID */
    private String recipientId;

    /** 触发者（点赞/收藏的人）用户 ID */
    private String actorId;

    /** 被互动的作品 ID；关注通知为空 */
    private String workId;

    /** 关联评论 ID；点赞、收藏和关注通知为空。 */
    private Long commentId;

    /** 评论摘要；普通互动通知为空。 */
    private String commentContent;

    /** 通知类型：like / favorite / follow */
    private String type;

    /** 是否已读：false=未读 / true=已读 */
    private Boolean isRead;

    /** 通知时间（服务端本地时区） */
    private LocalDateTime createdAt;
}
