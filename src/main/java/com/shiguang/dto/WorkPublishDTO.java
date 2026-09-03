package com.shiguang.dto;

import lombok.Data;

import java.util.List;

/**
 * 作品发布/保存草稿请求体（POST /api/works、PUT /api/works/{id} 共用）。
 *
 * 与 works 表的存储字段一一对应：
 * - status：draft=草稿（字段可缺省），published=发布（服务端做完整性校验）；
 * - contentMd：Markdown 富文本正文，媒体以已上传的 /uploads/... URL 引用；
 * - categoryIds：作品所属分类 id 列表（对应 categories.id），
 *   发布/保存草稿时随请求提交，服务端写入 work_categories 关联表；
 *   发布（published）时至少选择一个分类，草稿允许为空；
 * - tags：自由文本标签列表，独立于分类，一个作品可多个；可选。
 *   服务端做 trim、去空格、去重后写入 work_tags 关联表。
 */
@Data
public class WorkPublishDTO {

    private String status;

    private String title;

    private String coverUrl;

    private String contentMd;

    private String videoUrl;

    /** 纬度（WGS84），可选。给定时应与 longitude 成对出现。 */
    private Double latitude;

    /** 经度（WGS84），可选。给定时应与 latitude 成对出现。 */
    private Double longitude;

    /** 位置展示名，如"杭州·西湖"；可选，空/缺省表示不带位置。 */
    private String locationName;

    private List<Long> categoryIds;

    /** 自由文本标签列表，可选；空列表或 null 表示不带标签。 */
    private List<String> tags;
}
