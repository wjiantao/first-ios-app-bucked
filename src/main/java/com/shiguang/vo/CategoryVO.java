package com.shiguang.vo;

import lombok.Builder;
import lombok.Data;

/**
 * 分类视图对象（分类列表接口返回）。
 *
 * 对应 categories 表；status=1 表示启用，列表接口默认只返回启用分类。
 */
@Data
@Builder
public class CategoryVO {

    private Long id;

    private String code;

    private String name;

    private Integer sortOrder;
}
