-- 「拾光」推送设备表
-- 使用方式：mysql -uroot -p123456 shiguang < sql/push_devices.sql（或直接 mysql -uroot -p123456 < sql/push_devices.sql）
-- 说明：可重复执行，不会破坏已有数据。
--       记录每个用户注册的推送设备：iOS/Android 统一使用 JPush registrationID，
--       由后端按 vendor=jpush 调用极光服务端推送接口。
--       同一用户同一平台同一 vendor 只保留一个 token，重复注册覆盖旧 token。

USE shiguang;

CREATE TABLE IF NOT EXISTS push_devices (
    id         BIGINT      NOT NULL AUTO_INCREMENT COMMENT '设备记录ID',
    user_id    VARCHAR(64) NOT NULL COMMENT '所属用户ID，逻辑关联 users.id，不建外键（与现有表风格一致）',
    platform   VARCHAR(16) NOT NULL COMMENT '设备平台：ios / android',
    vendor     VARCHAR(16) NOT NULL DEFAULT 'jpush' COMMENT '推送厂商：当前仅支持 jpush',
    token      VARCHAR(512) NOT NULL COMMENT '推送 token（JPush registrationID）',
    created_at DATETIME    NOT NULL COMMENT '首次注册时间',
    updated_at DATETIME    NOT NULL COMMENT '最近更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_push_devices_user_platform_vendor (user_id, platform, vendor)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '推送设备表';
