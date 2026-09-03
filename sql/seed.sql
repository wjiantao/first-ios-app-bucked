-- 「拾光」种子数据（邮箱注册 + 第三方登录演示账号）
-- 使用方式：mysql -uroot -p shiguang < seed.sql（建议在 schema.sql 之后执行）

USE shiguang;

-- 演示账号：微信/抖音/Apple 登录在开发模式使用固定 mock uid 命中以下账号
INSERT IGNORE INTO users (id, email, password_hash, nickname, status, bio, avatar_url, created_at, updated_at) VALUES
('u-1002', NULL, NULL, '微信用户', 'active', '', NULL, '2026-08-01 10:00:00', '2026-08-01 10:00:00'),
('u-1003', NULL, NULL, '抖音用户', 'active', '', NULL, '2026-08-01 10:00:00', '2026-08-01 10:00:00'),
('u-1004', NULL, NULL, 'Apple 用户', 'active', '', NULL, '2026-08-01 10:00:00', '2026-08-01 10:00:00');

INSERT IGNORE INTO auth_accounts (user_id, channel, channel_uid, created_at) VALUES
('u-1002', 'wechat', 'mock-wechat', '2026-08-01 10:00:00'),
('u-1003', 'douyin', 'mock-douyin', '2026-08-01 10:00:00'),
('u-1004', 'apple', 'mock-apple', '2026-08-01 10:00:00');
