-- 「拾光」作品分类表 + 作品-分类关联表
-- 使用方式：mysql -uroot -p123456 < sql/categories.sql（需先执行 sql/works.sql）
-- 说明：可重复执行，不会破坏已有数据；作品与分类为多对多，一个作品可属于多个分类。

USE shiguang;

CREATE TABLE IF NOT EXISTS categories (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '分类ID',
    code        VARCHAR(32)  NOT NULL COMMENT '分类唯一标识：travel / food / photography / daily',
    name        VARCHAR(32)  NOT NULL COMMENT '展示名称：旅行 / 美食 / 摄影 / 日常',
    sort_order  INT          NOT NULL DEFAULT 0 COMMENT '展示排序，越小越靠前',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '1=启用，0=停用',
    created_at  DATETIME     NOT NULL,
    updated_at  DATETIME     NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_categories_code (code)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '作品分类表';

-- 「推荐 / 关注」是首页信息流视图而非内容分类，不落库；
-- 只预置与客户端分类胶囊对应的 4 个真实内容分类。
INSERT IGNORE INTO categories (code, name, sort_order, status, created_at, updated_at) VALUES
    ('travel',       '旅行', 1, 1, NOW(), NOW()),
    ('food',         '美食', 2, 1, NOW(), NOW()),
    ('photography',  '摄影', 3, 1, NOW(), NOW()),
    ('daily',        '日常', 4, 1, NOW(), NOW());

CREATE TABLE IF NOT EXISTS work_categories (
    work_id      VARCHAR(64) NOT NULL COMMENT '作品ID，逻辑关联 works.id，不建外键（与现有表风格一致）',
    category_id  BIGINT      NOT NULL COMMENT '分类ID，逻辑关联 categories.id',
    PRIMARY KEY (work_id, category_id),
    KEY idx_work_categories_category (category_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '作品-分类关联表';
