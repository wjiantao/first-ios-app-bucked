package com.shiguang.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 关注操作结果。
 *
 * 返回操作后的关注状态和最新粉丝数，客户端不根据旧值自行推算，
 * 避免并发请求或重复点击造成界面状态与数据库不一致。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "关注操作结果")
public class FollowResultVO {

    @Schema(description = "当前用户是否已关注目标用户", example = "true")
    private boolean following;

    @Schema(description = "目标用户的最新粉丝数", example = "12")
    private long followerCount;
}
