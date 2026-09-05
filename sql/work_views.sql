-- 「拾光」作品浏览记录表
-- 使用方式：mysql -uroot -p123456 < sql/work_views.sql（需先执行 sql/works.sql）
-- 说明：可重复执行，不会破坏已有数据。
--       每条浏览=一次作品详情打开（GET /api/works/{id}），供创作中心「数据概览」的
--       累计浏览次数与今日新增浏览次数聚合使用，也与现有表风格一致（不建外键）。
--       作者本人打开自己的作品不落记录，避免被自刷放大。

USE shiguang;

CREATE TABLE IF NOT EXISTS work_views (
    id         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '浏览记录ID',
    work_id    VARCHAR(64)  NOT NULL COMMENT '被浏览作品ID，逻辑关联 works.id，不建外键（与现有表风格一致）',
    viewer_id  VARCHAR(64)  NOT NULL COMMENT '浏览者用户ID，逻辑关联 users.id；详情接口需登录，故恒有值',
    created_at DATETIME     NOT NULL COMMENT '浏览时间（服务端本地时区）',
    PRIMARY KEY (id),
    KEY idx_work_views_work_created (work_id, created_at),
    KEY idx_work_views_created (created_at)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '作品浏览记录表';
