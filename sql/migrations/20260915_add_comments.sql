USE shiguang;

CREATE TABLE IF NOT EXISTS comments (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '评论 ID',
    work_id     VARCHAR(64)  NOT NULL COMMENT '作品 ID',
    author_id   VARCHAR(64)  NOT NULL COMMENT '评论作者 ID',
    parent_id   BIGINT       NULL COMMENT '父评论 ID，允许任意深度回复',
    content     VARCHAR(1000) NOT NULL DEFAULT '' COMMENT '评论文字内容',
    image_url   VARCHAR(512) NULL COMMENT '评论图片地址，最多一张',
    status      VARCHAR(16)  NOT NULL DEFAULT 'active' COMMENT 'active=正常 / deleted=已删除',
    created_at  DATETIME     NOT NULL,
    deleted_at  DATETIME     NULL,
    PRIMARY KEY (id),
    KEY idx_comments_work_created (work_id, created_at, id),
    KEY idx_comments_parent_created (parent_id, created_at, id),
    KEY idx_comments_author (author_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '作品评论';

ALTER TABLE notifications ADD COLUMN IF NOT EXISTS comment_id BIGINT NULL COMMENT '关联评论 ID' AFTER work_id;
ALTER TABLE notifications ADD COLUMN IF NOT EXISTS comment_content VARCHAR(255) NULL COMMENT '评论摘要' AFTER comment_id;
