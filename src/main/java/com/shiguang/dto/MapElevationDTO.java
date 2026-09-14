package com.shiguang.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/** 批量高程请求。 */
@Data
@Schema(description = "地图高程查询请求")
public class MapElevationDTO {
    private List<Point> points;

    @Data
    public static class Point {
        private Double longitude;
        private Double latitude;
    }
}
