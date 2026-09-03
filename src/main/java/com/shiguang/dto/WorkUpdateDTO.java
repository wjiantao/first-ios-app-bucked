package com.shiguang.dto;

import lombok.Data;

import java.util.List;

/**
 * 修改作品请求体（PUT /api/works/{id}）。
 *
 * 局部更新语义：标量字段（status/title/coverUrl/contentMd/videoUrl）传 null 表示保持不变，
 * 只更新传入的非空字段；列表字段（categoryIds/tags）传 null 表示保持不变，
 * 传空数组 [] 表示清空。
 */
@Data
public class WorkUpdateDTO {

    /** draft=保存草稿 / published=发布（发布时服务端做完整性校验）。 */
    private String status;

    private String title;

    private String coverUrl;

    private String contentMd;

    private String videoUrl;

    /** 纬度（WGS84），可选。locationName 为 null 时保持不变；清空位置时置 null。 */
    private Double latitude;

    /** 经度（WGS84），可选。locationName 为 null 时保持不变；清空位置时置 null。 */
    private Double longitude;

    /** 位置展示名；null=保持不变，空字符串=清空位置，非空=更新位置。 */
    private String locationName;

    /** 作品所属分类 id 列表；null=保持不变，空数组=清空。 */
    private List<Long> categoryIds;

    /** 自由文本标签列表；null=保持不变，空数组=清空。 */
    private List<String> tags;
}
