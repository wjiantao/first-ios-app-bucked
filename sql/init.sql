-- 「拾光」服务端完整初始化脚本（Render / 全新 MySQL 8 实例一次性执行）
-- 说明：
--   1. 所有语句均可重复执行（CREATE TABLE IF NOT EXISTS / INSERT IGNORE），
--      因此既可作为 MySQL 容器首次启动的 init 脚本，也可手动导入已有库。
--   2. 覆盖本地 shiguang 库的全部业务表，避免部署时缺失基础表。

CREATE DATABASE IF NOT EXISTS shiguang
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE shiguang;

-- ---------------------------------------------------------------------------
-- 用户与第三方登录
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id            VARCHAR(64)  NOT NULL COMMENT '用户ID（与 RN 端约定为字符串）',
    email         VARCHAR(128) NULL COMMENT '登录邮箱（唯一）',
    password_hash VARCHAR(255) NULL COMMENT 'PBKDF2 加盐密码摘要，格式 iterations:salt:hash',
    nickname      VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '昵称',
    status        VARCHAR(16)  NOT NULL DEFAULT 'active' COMMENT '账号状态：pending=邮箱已验证待设置密码，active=可登录',
    bio           VARCHAR(255) NOT NULL DEFAULT '' COMMENT '个人简介',
    avatar_url    VARCHAR(512) NULL COMMENT '头像地址',
    created_at    DATETIME     NOT NULL,
    updated_at    DATETIME     NOT NULL,
    tagline       VARCHAR(255) NULL COMMENT '签名',
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_email (email)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户表';

CREATE TABLE IF NOT EXISTS auth_accounts (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    user_id     VARCHAR(64)  NOT NULL COMMENT '所属用户',
    channel     VARCHAR(16)  NOT NULL COMMENT '第三方渠道：wechat/douyin/apple',
    channel_uid VARCHAR(128) NOT NULL COMMENT '第三方渠道内的用户标识',
    created_at  DATETIME     NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_channel_uid (channel, channel_uid),
    KEY idx_auth_user (user_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '第三方登录账号绑定表';

CREATE TABLE IF NOT EXISTS user_follows (
    follower_id  VARCHAR(64) NOT NULL COMMENT '发起关注的用户 ID',
    following_id VARCHAR(64) NOT NULL COMMENT '被关注的用户 ID',
    created_at   DATETIME    NOT NULL COMMENT '关注时间',
    PRIMARY KEY (follower_id, following_id),
    KEY idx_user_follows_following_created (following_id, created_at),
    KEY idx_user_follows_follower_created (follower_id, created_at)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户关注关系表';

-- ---------------------------------------------------------------------------
-- 作品
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS works (
    id            VARCHAR(64)   NOT NULL COMMENT '作品ID：w- 前缀 + 12位随机串',
    author_id     VARCHAR(64)   NOT NULL COMMENT '作者用户ID，逻辑关联 users.id',
    type          VARCHAR(16)   NOT NULL DEFAULT 'article' COMMENT '作品类型：image_text / article / video',
    title         VARCHAR(100)  NOT NULL DEFAULT '' COMMENT '标题',
    cover_url     VARCHAR(512)  NULL COMMENT '封面图URL',
    video_url     VARCHAR(512)  NULL COMMENT '视频文件URL（预留）',
    latitude      DOUBLE        NULL COMMENT '纬度（WGS84）',
    longitude     DOUBLE        NULL COMMENT '经度（WGS84）',
    location_name VARCHAR(200)  NULL COMMENT '位置展示名',
    content_md    MEDIUMTEXT    NULL COMMENT 'Markdown 正文（源格式）',
    content_text  MEDIUMTEXT    NULL COMMENT '纯文本正文（列表摘要/搜索）',
    status        VARCHAR(16)   NOT NULL DEFAULT 'draft' COMMENT 'draft / published / offline / deleted',
    published_at  DATETIME      NULL COMMENT '发布时间',
    deleted_at    DATETIME      NULL COMMENT '软删除时间',
    created_at    DATETIME      NOT NULL,
    updated_at    DATETIME      NOT NULL,
    PRIMARY KEY (id),
    KEY idx_works_author_status (author_id, status, published_at),
    KEY idx_works_title (title),
    KEY idx_works_status_location (status, latitude, longitude)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '作品表';

-- ---------------------------------------------------------------------------
-- 分类 / 标签 / 作品关联
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS categories (
    id         BIGINT      NOT NULL AUTO_INCREMENT COMMENT '分类ID',
    code       VARCHAR(32) NOT NULL COMMENT '分类唯一标识',
    name       VARCHAR(32) NOT NULL COMMENT '展示名称',
    sort_order INT         NOT NULL DEFAULT 0 COMMENT '展示排序，越小越靠前',
    status     TINYINT     NOT NULL DEFAULT 1 COMMENT '1=启用，0=停用',
    created_at DATETIME    NOT NULL,
    updated_at DATETIME    NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_categories_code (code)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '作品分类表';

CREATE TABLE IF NOT EXISTS work_categories (
    work_id     VARCHAR(64) NOT NULL COMMENT '作品ID',
    category_id BIGINT      NOT NULL COMMENT '分类ID',
    PRIMARY KEY (work_id, category_id),
    KEY idx_work_categories_category (category_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '作品-分类关联表';

CREATE TABLE IF NOT EXISTS tags (
    id         BIGINT      NOT NULL AUTO_INCREMENT COMMENT '标签ID',
    name       VARCHAR(30) NOT NULL COMMENT '标签名',
    created_at DATETIME    NOT NULL COMMENT '首次出现时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_tags_name (name)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '作品标签表';

CREATE TABLE IF NOT EXISTS work_tags (
    work_id VARCHAR(64) NOT NULL COMMENT '作品ID',
    tag_id  BIGINT      NOT NULL COMMENT '标签ID',
    PRIMARY KEY (work_id, tag_id),
    KEY idx_work_tags_tag (tag_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '作品-标签关联表';

-- ---------------------------------------------------------------------------
-- 点赞 / 收藏 / 浏览
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS work_likes (
    user_id    VARCHAR(64) NOT NULL COMMENT '点赞用户ID',
    work_id    VARCHAR(64) NOT NULL COMMENT '作品ID',
    created_at DATETIME    NOT NULL COMMENT '点赞时间',
    PRIMARY KEY (user_id, work_id),
    KEY idx_work_likes_work_id (work_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '作品点赞表';

CREATE TABLE IF NOT EXISTS map_checkins (
    id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '打卡记录 ID',
    user_id       VARCHAR(64)  NOT NULL COMMENT '用户 ID',
    work_id       VARCHAR(64)  NOT NULL COMMENT '作品 ID',
    latitude      DOUBLE       NOT NULL COMMENT '打卡纬度（GCJ-02）',
    longitude     DOUBLE       NOT NULL COMMENT '打卡经度（GCJ-02）',
    location_name VARCHAR(200) NULL COMMENT '打卡位置名称',
    created_at    DATETIME     NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_map_checkin_user_work (user_id, work_id),
    KEY idx_map_checkins_user_created (user_id, created_at)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '地图作品打卡记录';

CREATE TABLE IF NOT EXISTS work_favorites (
    user_id    VARCHAR(64) NOT NULL COMMENT '收藏用户ID',
    work_id    VARCHAR(64) NOT NULL COMMENT '作品ID',
    created_at DATETIME    NOT NULL COMMENT '收藏时间',
    PRIMARY KEY (user_id, work_id),
    KEY idx_work_favorites_work_id (work_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '作品收藏表';

CREATE TABLE IF NOT EXISTS work_views (
    id         BIGINT      NOT NULL AUTO_INCREMENT COMMENT '浏览记录ID',
    work_id    VARCHAR(64) NOT NULL COMMENT '被浏览作品ID',
    viewer_id  VARCHAR(64) NOT NULL COMMENT '浏览者用户ID',
    created_at DATETIME    NOT NULL COMMENT '浏览时间',
    PRIMARY KEY (id),
    KEY idx_work_views_work_created (work_id, created_at),
    KEY idx_work_views_created (created_at)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '作品浏览记录表';

-- ---------------------------------------------------------------------------
-- 站内通知 / 推送设备
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS notifications (
    id           BIGINT      NOT NULL AUTO_INCREMENT COMMENT '通知ID',
    recipient_id VARCHAR(64) NOT NULL COMMENT '接收者（作品作者）用户ID',
    actor_id     VARCHAR(64) NOT NULL COMMENT '触发者用户ID',
    work_id      VARCHAR(64) NULL COMMENT '被互动作品ID；关注通知为空',
    type         VARCHAR(16) NOT NULL COMMENT 'like=点赞 / favorite=收藏 / follow=关注',
    is_read      TINYINT(1)  NOT NULL DEFAULT 0 COMMENT '0=未读 / 1=已读',
    created_at   DATETIME    NOT NULL COMMENT '通知时间',
    PRIMARY KEY (id),
    KEY idx_notifications_recipient_created (recipient_id, created_at),
    KEY idx_notifications_recipient_read (recipient_id, is_read)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '点赞/收藏通知表';

CREATE TABLE IF NOT EXISTS push_devices (
    id         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '设备记录ID',
    user_id    VARCHAR(64)  NOT NULL COMMENT '所属用户ID',
    platform   VARCHAR(16)  NOT NULL COMMENT 'ios / android',
    vendor     VARCHAR(16)  NOT NULL DEFAULT 'jpush' COMMENT '推送厂商',
    token      VARCHAR(512) NOT NULL COMMENT 'JPush registrationID',
    created_at DATETIME     NOT NULL COMMENT '首次注册时间',
    updated_at DATETIME     NOT NULL COMMENT '最近更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_push_devices_user_platform_vendor (user_id, platform, vendor)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '推送设备表';

-- ---------------------------------------------------------------------------
-- 种子数据
-- ---------------------------------------------------------------------------
INSERT IGNORE INTO categories (code, name, sort_order, status, created_at, updated_at) VALUES
    ('travel',      '旅行', 1, 1, NOW(), NOW()),
    ('food',        '美食', 2, 1, NOW(), NOW()),
    ('photography', '摄影', 3, 1, NOW(), NOW()),
    ('daily',       '日常', 4, 1, NOW(), NOW());

INSERT IGNORE INTO users (id, email, password_hash, nickname, status, bio, avatar_url, created_at, updated_at) VALUES
    ('u-1002', NULL, NULL, '微信用户', 'active', '', NULL, '2026-08-01 10:00:00', '2026-08-01 10:00:00'),
    ('u-1003', NULL, NULL, '抖音用户', 'active', '', NULL, '2026-08-01 10:00:00', '2026-08-01 10:00:00'),
    ('u-1004', NULL, NULL, 'Apple 用户', 'active', '', NULL, '2026-08-01 10:00:00', '2026-08-01 10:00:00');

INSERT IGNORE INTO auth_accounts (user_id, channel, channel_uid, created_at) VALUES
    ('u-1002', 'wechat', 'mock-wechat', '2026-08-01 10:00:00'),
    ('u-1003', 'douyin', 'mock-douyin', '2026-08-01 10:00:00'),
    ('u-1004', 'apple',   'mock-apple',  '2026-08-01 10:00:00');
