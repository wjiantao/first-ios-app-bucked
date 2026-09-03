package com.shiguang.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 附近作品查询请求体（POST /api/works/nearby）。
 *
 * 以 (latitude, longitude) 为圆心、radiusKm 为半径（公里），
 * 返回已发布且带坐标的作品，按距离由近到远排序。
 * current/pageSize 缺省为 1/10，radiusKm 缺省为 5 公里。
 */
@Data
@Schema(description = "附近作品查询请求（POST /api/works/nearby）")
public class NearbyWorksQueryDTO {

    @Schema(description = "页码，从 1 开始", example = "1", defaultValue = "1")
    private Integer current = 1;

    @Schema(description = "每页条数", example = "10", defaultValue = "10")
    private Integer pageSize = 10;

    @Schema(description = "中心纬度（WGS84），必填", example = "30.2469")
    private Double latitude;

    @Schema(description = "中心经度（WGS84），必填", example = "120.1551")
    private Double longitude;

    @Schema(description = "搜索半径（公里），缺省 5", example = "10", defaultValue = "5")
    private Double radiusKm;
}
