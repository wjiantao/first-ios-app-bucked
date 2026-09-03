package com.shiguang.vo;

import lombok.Builder;
import lombok.Data;

/**
 * 作品-分类查询结果行（Mapper 内部使用，不分页返回给客户端）。
 *
 * 用于按作品 ID 批量查询其所属分类：workId 标记分类归属于哪条作品。
 */
@Data
@Builder
public class WorkCategoryVO {

    private String workId;

    private Long id;

    private String code;

    private String name;

    private Integer sortOrder;
}
