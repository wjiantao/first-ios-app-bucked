package com.shiguang.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/** 地图作品打卡请求。坐标统一使用高德 GCJ-02。 */
@Data
@Schema(description = "地图作品打卡请求")
public class MapCheckinDTO {

    @Schema(description = "作品 ID", example = "w-abc123", required = true)
    private String workId;

    @Schema(description = "打卡纬度（GCJ-02）", example = "36.6512", required = true)
    private Double latitude;

    @Schema(description = "打卡经度（GCJ-02）", example = "117.1201", required = true)
    private Double longitude;

    @Schema(description = "位置名称", example = "济南·大明湖")
    private String locationName;
}
