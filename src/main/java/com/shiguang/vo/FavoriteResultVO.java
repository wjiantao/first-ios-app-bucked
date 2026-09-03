package com.shiguang.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 收藏操作结果视图对象（POST/DELETE /api/works/{id}/favorite 共用）。
 *
 * 与点赞的 WorkLikeResultVO 保持同一结构：
 * 返回“操作后是否处于收藏状态 + 操作后的收藏总数”，
 * 客户端拿到后可直接更新书签选中态与计数，无需再发起一次查询。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "收藏操作结果（收藏/取消收藏共用）")
public class FavoriteResultVO {

    @Schema(description = "操作后是否处于收藏状态", example = "true")
    private boolean favorited;

    @Schema(description = "操作后的收藏总数", example = "12")
    private long favoriteCount;
}
