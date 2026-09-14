-- 用户关注关系与关注通知兼容迁移。

USE shiguang;

CREATE TABLE IF NOT EXISTS user_follows (
    follower_id  VARCHAR(64) NOT NULL COMMENT '发起关注的用户 ID',
    following_id VARCHAR(64) NOT NULL COMMENT '被关注的用户 ID',
    created_at   DATETIME    NOT NULL COMMENT '关注时间',
    PRIMARY KEY (follower_id, following_id),
    KEY idx_user_follows_following_created (following_id, created_at),
    KEY idx_user_follows_follower_created (follower_id, created_at)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户关注关系表';

ALTER TABLE notifications
    MODIFY COLUMN work_id VARCHAR(64) NULL COMMENT '被互动作品ID；关注通知为空';

