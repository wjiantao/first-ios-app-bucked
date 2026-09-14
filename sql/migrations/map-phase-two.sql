USE shiguang;

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
