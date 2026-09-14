package com.shiguang.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Cesium 地图作品标记视图对象。
 *
 * 只携带地球打点所需的轻量字段，避免复用 WorkVO 时把正文、分类、标签等
 * 大字段一并下发，降低地图接口在可视范围变化时的网络与解析开销。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Cesium 地图作品标记信息")
public class MapWorkVO {

    @Schema(description = "作品 ID")
    private String id;

    @Schema(description = "作品标题")
    private String title;

    @Schema(description = "作者昵称；账号昵称为空时由客户端显示统一兜底文案", nullable = true)
    private String authorName;

    @Schema(description = "作品类型：image_text / article / video")
    private String type;

    @Schema(description = "封面图 URL（/uploads/... 路径）", nullable = true)
    private String coverUrl;

    @Schema(description = "纬度（WGS84）")
    private Double latitude;

    @Schema(description = "经度（WGS84）")
    private Double longitude;

    @Schema(description = "位置展示名，如“杭州·西湖”", nullable = true)
    private String locationName;

    @Schema(description = "点赞数")
    private long likeCount;

    /**
     * 排序时间，仅用于服务端合并跨 180° 经线的两段查询结果。
     *
     * 前端接口不需要发布时间，因此加 @JsonIgnore 避免把它暴露到地图响应里，
     * 但服务层又需要用它把两段查询按时间重新合并成全局倒序。
     */
    @JsonIgnore
    private LocalDateTime sortTime;
}
