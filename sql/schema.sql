-- 「拾光」作品表
-- 使用方式：mysql -uroot -p123456 shiguang < sql/works.sql（或直接 mysql -uroot -p123456 < sql/works.sql）
-- 说明：可重复执行，不会破坏已有数据。

USE shiguang;

CREATE TABLE IF NOT EXISTS works (
    id            VARCHAR(64)   NOT NULL COMMENT '作品ID：w- 前缀 + 12位随机串，风格与 users.id 一致',
    author_id     VARCHAR(64)   NOT NULL COMMENT '作者用户ID，应用层关联 users.id，不建外键（与现有表风格一致）',
    type          VARCHAR(16)   NOT NULL DEFAULT 'article' COMMENT '作品类型：image_text / article / video（当前发布流程未开放，默认 article，与现有数据一致）',
    title         VARCHAR(100)  NOT NULL DEFAULT '' COMMENT '标题（发布必填，草稿可为空）',
    cover_url     VARCHAR(512)  NULL     COMMENT '封面图URL；图文/长文未显式提供时可存正文首图',
    video_url     VARCHAR(512)  NULL     COMMENT '视频文件URL（预留字段，当前发布流程不使用）',
    latitude      DOUBLE        NULL     COMMENT '纬度（WGS84），发布时可选随作品携带；不带则为 NULL',
    longitude     DOUBLE        NULL     COMMENT '经度（WGS84），发布时可选随作品携带；不带则为 NULL',
    location_name VARCHAR(200)  NULL     COMMENT '位置展示名，如"杭州·西湖"；发布时可选，不带则为 NULL',
    content_md    MEDIUMTEXT    NULL     COMMENT 'Markdown 富文本正文（源格式，唯一内容事实源）',
    content_text  MEDIUMTEXT    NULL     COMMENT 'Markdown 剥离后的纯文本，供列表摘要/未来全文搜索',
    status        VARCHAR(16)   NOT NULL DEFAULT 'draft' COMMENT 'draft=草稿 / published=已发布 / offline=下架 / deleted=软删除',
    published_at  DATETIME      NULL     COMMENT '发布时间，发布后置位，瀑布流按此倒序',
    deleted_at    DATETIME      NULL     COMMENT '软删除时间，置位后列表查询不可见',
    created_at    DATETIME      NOT NULL,
    updated_at    DATETIME      NOT NULL,
    PRIMARY KEY (id),
    KEY idx_works_author_status (author_id, status, published_at),
    KEY idx_works_title (title),
    KEY idx_works_status_location (status, latitude, longitude)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '作品表';
