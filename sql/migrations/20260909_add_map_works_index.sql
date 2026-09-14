-- 3D 地图接口索引迁移：为可视范围查询增加 (status, latitude, longitude) 组合索引。
-- 通过 information_schema 判断，避免在已有索引的库上重复执行 ALTER TABLE 报错。

USE shiguang;

SET @index_exists := (
    SELECT COUNT(*)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'works'
      AND index_name = 'idx_works_status_location'
);

SET @ddl := IF(
    @index_exists = 0,
    'ALTER TABLE works ADD INDEX idx_works_status_location (status, latitude, longitude)',
    'SELECT 1'
);

PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
