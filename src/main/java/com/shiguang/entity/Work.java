package com.shiguang.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 作品实体，对应 works 表。
 *
 * 作品与分类为多对多：分类不冗余在 works 中，而是通过
 * work_categories 关联表（WorkMapper.insertWorkCategories）维护。
 * status 取值 draft=草稿 / published=已发布 / offline=下架（原已发布作品被作者隐藏）/
 * deleted=软删除。
 */
@Data
public class Work {

    private String id;

    /** 作者用户 ID，应用层关联 users.id。 */
    private String authorId;

    private String title;

    private String coverUrl;

    private String videoUrl;

    /** 纬度（WGS84），可选；与 {@link #longitude}、{@link #locationName} 组成作品地理位置。 */
    private Double latitude;

    /** 经度（WGS84），可选；与 {@link #latitude}、{@link #locationName} 组成作品地理位置。 */
    private Double longitude;

    /** 位置展示名，如"杭州·西湖"；可选，为空表示作品不带位置。 */
    private String locationName;

    /** Markdown 富文本正文（源格式）。 */
    private String contentMd;

    /** Markdown 剥离后的纯文本，供列表摘要/未来全文搜索。 */
    private String contentText;

    private String status;

    private LocalDateTime publishedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
