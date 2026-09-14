package com.shiguang.vo;

import lombok.Builder;
import lombok.Value;

/** 发布页地点候选。 */
@Value
@Builder
public class LocationSearchVO {
    String id;
    String name;
    String address;
    Double latitude;
    Double longitude;
}
