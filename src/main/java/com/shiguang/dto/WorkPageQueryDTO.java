package com.shiguang.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 作品分页查询请求体（POST /api/works/page）。
 *
 * current/pageSize 缺省为 1/10；categoryId/status/keyword 不传表示不筛选。
 * keyword 按标题模糊匹配，status 取值 draft/published/offline；不传时默认不返回 deleted 与 offline。
 * tag 按标签名精确匹配（自由文本标签），不传表示不按标签筛选。
 */
@Data
@Schema(description = "作品分页查询请求（POST /api/works/page）")
public class WorkPageQueryDTO {

    /** 页码，从 1 开始，缺省 1。 */
    @Schema(description = "页码，从 1 开始", example = "1", defaultValue = "1")
    private Integer current = 1;

    /** 每页条数，缺省 10。 */
    @Schema(description = "每页条数", example = "10", defaultValue = "10")
    private Integer pageSize = 10;

    /** 分类筛选：对应 categories.id，为空表示全部分类。 */
    @Schema(description = "分类筛选：对应 categories.id，为空表示全部分类", example = "1")
    private Long categoryId;

    /** 状态筛选：draft=草稿 / published=已发布 / offline=下架；为空时默认不返回 deleted 与 offline。 */
    @Schema(description = "状态筛选：draft=草稿 / published=已发布 / offline=下架；为空时默认不返回 deleted 与 offline", example = "published")
    private String status;

    /** 标题关键词，模糊匹配；为空表示不按标题筛选。 */
    @Schema(description = "标题关键词，按标题模糊匹配；为空表示不筛选", example = "旅行")
    private String keyword;

    /** 标签筛选：标签名精确匹配，为空表示全部分类。 */
    @Schema(description = "标签筛选：标签名精确匹配，为空表示不按标签筛选", example = "落日")
    private String tag;
}
