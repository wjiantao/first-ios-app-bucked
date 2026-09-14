package com.shiguang.dto;

import lombok.Data;

/** Cesium 地点标签视野查询参数，坐标使用 WGS84。 */
@Data
public class MapLabelsQueryDTO {
    private Double west;
    private Double south;
    private Double east;
    private Double north;
    private Integer zoomLevel;
    private Integer limit = 200;
}
