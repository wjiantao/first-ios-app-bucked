package com.shiguang.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 作品视图对象（发布/详情/列表共用）。
 *
 * 对应 works 表：status 为 draft（草稿）/ published（已发布）/ offline（下架）/ deleted（已软删除）。
 * publishedAt 在发布时置位，createdAt/updatedAt 输出为 ISO 字符串（见 jackson 配置）。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "作品信息（发布/详情/列表共用）")
public class WorkVO {

    @Schema(description = "作品 ID", example = "w-1a2b3c4d5e6f")
    private String id;

    @Schema(description = "作者用户 ID", example = "u-1a2b3c4d5e6f", nullable = true)
    private String authorId;

    @Schema(description = "作者昵称", example = "拾光", nullable = true)
    private String authorNickname;

    @Schema(description = "作者头像 URL", example = "/uploads/avatar.jpg", nullable = true)
    private String authorAvatarUrl;

    @Schema(description = "当前用户是否关注作者", example = "false")
    private boolean following;

    @Schema(description = "作者粉丝数", example = "12")
    private long followerCount;

    @Schema(description = "作品所属分类（启用中的分类，按 sortOrder 升序）")
    private List<CategoryVO> categories;

    @Schema(description = "作品标签（自由文本，按名称升序；无标签时为空数组）")
    private List<String> tags;

    @Schema(description = "点赞数", example = "0")
    private long likeCount;

    @Schema(description = "当前登录用户是否已点赞；列表接口对游客开放时恒为 false", example = "false")
    private boolean liked;

    @Schema(description = "收藏数", example = "0")
    private long favoriteCount;

    @Schema(description = "当前登录用户是否已收藏；列表接口对游客开放时恒为 false", example = "false")
    private boolean favorited;

    @Schema(description = "标题", example = "周末的湖边日落")
    private String title;

    @Schema(description = "作品类型：image_text=图文 / article=长文 / video=视频", example = "article")
    private String type;

    @Schema(description = "封面图 URL（/uploads/... 路径）", example = "/uploads/3f9c2a1b4d5e6f7a8b9c0d1e2f3a4b5c.jpg", nullable = true)
    private String coverUrl;

    @Schema(description = "视频 URL（预留字段，当前发布流程不使用）", example = "/uploads/video.mp4", nullable = true)
    private String videoUrl;

    @Schema(description = "纬度（WGS84）；作品未带位置时为 null", example = "30.2469", nullable = true)
    private Double latitude;

    @Schema(description = "经度（WGS84）；作品未带位置时为 null", example = "120.1551", nullable = true)
    private Double longitude;

    @Schema(description = "位置展示名，如\"杭州·西湖\"；作品未带位置时为 null", example = "杭州·西湖", nullable = true)
    private String locationName;

    @Schema(description = "与查询中心的距离（公里，保留 4 位）；仅“附近的作品”接口返回，其他场景为 null",
            example = "1.2345", nullable = true)
    private Double distance;

    @Schema(description = "Markdown 富文本正文，媒体以已上传的 /uploads/... URL 引用", nullable = true)
    private String contentMd;

    @Schema(description = "状态：draft=草稿 / published=已发布 / offline=下架 / deleted=已删除", example = "published")
    private String status;

    @Schema(description = "发布时间（ISO 8601），草稿为 null", example = "2026-09-02T10:20:30")
    private LocalDateTime publishedAt;

    @Schema(description = "创建时间（ISO 8601）", example = "2026-09-01T08:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间（ISO 8601）", example = "2026-09-02T10:20:30")
    private LocalDateTime updatedAt;

    @Schema(description = "收藏时间（ISO 8601）；仅“我的收藏”列表返回，其他场景为 null", example = "2026-09-03T10:20:30", nullable = true)
    private LocalDateTime favoritedAt;

    @Schema(description = "点赞时间（ISO 8601）；仅“我的喜欢”列表返回，其他场景为 null", example = "2026-09-03T10:20:30", nullable = true)
    private LocalDateTime likedAt;
}
