package com.shiguang.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 通知视图对象（站内消息中心列表）。
 *
 * actor 为触发互动的人，work 为可选的被互动作品；关注通知不关联作品。
 * isRead 表示该通知是否已被接收者阅读。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "通知信息（站内消息中心）")
public class NotificationVO {

    @Schema(description = "通知 ID", example = "1")
    private Long id;

    @Schema(description = "接收者用户 ID", example = "u-1a2b3c4d5e6f")
    private String recipientId;

    @Schema(description = "触发者（点赞/收藏/关注的人）用户 ID", example = "u-1a2b3c4d5e6f")
    private String actorId;

    @Schema(description = "触发者昵称", example = "拾光", nullable = true)
    private String actorNickname;

    @Schema(description = "触发者头像 URL", example = "/uploads/xxx.jpg", nullable = true)
    private String actorAvatarUrl;

    @Schema(description = "被互动的作品 ID；关注通知为空", example = "w-1a2b3c4d5e6f", nullable = true)
    private String workId;

    @Schema(description = "作品标题；关注通知为空，作品已删除时为“作品已删除”", example = "周末的湖边日落", nullable = true)
    private String workTitle;

    @Schema(description = "作品封面 URL", example = "/uploads/xxx.jpg", nullable = true)
    private String workCoverUrl;

    @Schema(description = "通知类型：like=点赞 / favorite=收藏 / follow=关注", example = "like")
    private String type;

    @Schema(description = "是否已读", example = "false")
    private Boolean isRead;

    @Schema(description = "通知时间（ISO 8601）", example = "2026-09-04T10:20:30")
    private LocalDateTime createdAt;
}
