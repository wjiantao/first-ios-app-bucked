-- 「拾光」通知表
-- 使用方式：mysql -uroot -p123456 shiguang < sql/notifications.sql（或直接 mysql -uroot -p123456 < sql/notifications.sql）
-- 说明：可重复执行，不会破坏已有数据。
--       当他人对某篇「已发布」作品点赞/收藏时，给作品作者写入一条通知，供站内消息中心展示，
--       并在作者注册设备后进行 iOS/Android 系统推送。取消点赞/收藏不删除历史通知。

USE shiguang;

CREATE TABLE IF NOT EXISTS notifications (
    id           BIGINT      NOT NULL AUTO_INCREMENT COMMENT '通知ID',
    recipient_id VARCHAR(64) NOT NULL COMMENT '接收者（作品作者）用户ID，逻辑关联 users.id，不建外键（与现有表风格一致）',
    actor_id     VARCHAR(64) NOT NULL COMMENT '触发者（点赞/收藏的人）用户ID，逻辑关联 users.id，不建外键',
    work_id      VARCHAR(64) NULL COMMENT '被互动的作品ID；关注通知为空',
    type         VARCHAR(16) NOT NULL COMMENT '通知类型：like=点赞 / favorite=收藏 / follow=关注',
    is_read      TINYINT(1)  NOT NULL DEFAULT 0 COMMENT '是否已读：0=未读 / 1=已读',
    created_at   DATETIME    NOT NULL COMMENT '通知时间（服务端本地时区）',
    PRIMARY KEY (id),
    KEY idx_notifications_recipient_created (recipient_id, created_at),
    KEY idx_notifications_recipient_read (recipient_id, is_read)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '点赞/收藏通知表';
