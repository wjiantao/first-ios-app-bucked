package com.shiguang.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创作者数据概览（GET /api/creator/stats）。
 *
 * 只返回给「创作中心」卡片展示所需的原始整型计数，展示格式化（如 2.4w / 1.8k）由 RN 前端负责。
 * 统计口径：仅统计当前用户已发布与下架的作品（不含草稿与已删除），
 * 点赞取自 work_likes、浏览取自 work_views；
 * 本轮不返回粉丝数（followers），因为关注关系尚未实现。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "创作者数据概览")
public class CreatorStatsVO {

    @Schema(description = "累计浏览量（当前用户所有已发布/下架作品收到的浏览总数）", example = "24000")
    private long views;

    @Schema(description = "今日新增浏览量（服务端本地时区当日，自然日 0 点起）", example = "1286")
    private long todayViews;

    @Schema(description = "累计点赞数（当前用户所有已发布/下架作品收获的点赞总数）", example = "8620")
    private long likes;
}
