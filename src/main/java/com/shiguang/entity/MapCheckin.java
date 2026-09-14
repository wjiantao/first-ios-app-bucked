package com.shiguang.entity;

import lombok.Data;

import java.time.LocalDateTime;

/** 地图打卡记录，对应 map_checkins 表。 */
@Data
public class MapCheckin {
    private Long id;
    private String userId;
    private String workId;
    private Double latitude;
    private Double longitude;
    private String locationName;
    private LocalDateTime createdAt;
}
