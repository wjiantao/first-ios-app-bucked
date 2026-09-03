-- 「拾光」作品标签表 + 作品-标签关联表
-- 使用方式：mysql -uroot -p123456 < sql/tags.sql（需先执行 sql/works.sql）
-- 说明：可重复执行，不会破坏已有数据；标签为自由文本、独立于固定分类（categories），
--       作品与标签为多对多：一个作品可带多个标签，一个标签可被多个作品引用。
--       与现有表风格一致：应用层逻辑关联，不建外键。

USE shiguang;

CREATE TABLE IF NOT EXISTS tags (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '标签ID',
    name        VARCHAR(30)  NOT NULL COMMENT '标签名（trim 后去重；utf8mb4_unicode_ci 下 ASCII 大小写不敏感）',
    created_at  DATETIME     NOT NULL COMMENT '首次出现时间（随作品发布/编辑时注入）',
    PRIMARY KEY (id),
    UNIQUE KEY uk_tags_name (name)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '作品标签表';

CREATE TABLE IF NOT EXISTS work_tags (
    work_id      VARCHAR(64) NOT NULL COMMENT '作品ID，逻辑关联 works.id，不建外键（与现有表风格一致）',
    tag_id       BIGINT      NOT NULL COMMENT '标签ID，逻辑关联 tags.id',
    PRIMARY KEY (work_id, tag_id),
    KEY idx_work_tags_tag (tag_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '作品-标签关联表';
