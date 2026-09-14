package com.shiguang.dto;

import lombok.Data;

/** 发布页地点搜索请求。 */
@Data
public class LocationSearchDTO {
    private String keyword;
    private Integer limit;
}
