-- 「拾光」push_devices 表 v2 迁移脚本
-- 使用方式：mysql -uroot -p123456 shiguang < sql/push_devices_v2_migration.sql
-- 说明：仅对已经创建过旧版 push_devices 表的数据库执行一次。
--       旧版 token 是 APNs/FCM token，不能用于 JPush，因此迁移后清理旧数据，
--       由新客户端登录后重新注册 JPush registrationID。

USE shiguang;

ALTER TABLE
    ADD COLUMN vendor VARCHAR(16) NOT NULL DEFAULT 'jpush' COMMENT '推送厂商：当前仅支持 jpush'
    AFTER platform;

DELETE FROM push_devices;

ALTER TABLE push_devices
    DROP INDEX uk_push_devices_user_platform;

ALTER TABLE push_devices
    ADD UNIQUE KEY uk_push_devices_user_platform_vendor (user_id, platform, vendor);
