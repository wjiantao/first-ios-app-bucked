package com.shiguang.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 通知实体，对应 notifications 表。
 *
 * 当他人点赞/收藏某篇「已发布」作品时，给作品作者（recipient）写入一条通知；
 * 取消点赞/收藏不删除历史通知。type 取值：like=点赞 / favorite=收藏。
 */
@Data
public class Notification {

    private Long id;

    /** 接收者（作品作者）用户 ID */
    private String recipientId;

    /** 触发者（点赞/收藏的人）用户 ID */
    private String actorId;

    /** 被互动的作品 ID */
    private String workId;

    /** 通知类型：like / favorite */
    private String type;

    /** 是否已读：false=未读 / true=已读 */
    private Boolean isRead;

    /** 通知时间（服务端本地时区） */
    private LocalDateTime createdAt;
}
