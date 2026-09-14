package com.shiguang.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Cesium 地图可视范围作品查询请求体。
 *
 * Cesium 相机移动结束后会把当前 WGS84 可视矩形换算成 west/south/east/north，
 * 服务端只返回该矩形内已发布且带坐标的作品，避免一次性把全库作品推给移动端。
 */
@Data
@Schema(description = "Cesium 地图可视范围作品查询请求（POST /api/map/works，WGS84）")
public class MapWorksQueryDTO {

    @Schema(description = "可视范围西边界经度（WGS84），必填", example = "116.0")
    private Double west;

    @Schema(description = "可视范围南边界纬度（WGS84），必填", example = "28.0")
    private Double south;

    @Schema(description = "可视范围东边界经度（WGS84），必填", example = "122.0")
    private Double east;

    @Schema(description = "可视范围北边界纬度（WGS84），必填", example = "32.0")
    private Double north;

    @Schema(description = "最多返回条数，默认 500，最大 1000", example = "500")
    private Integer limit = 500;
}
