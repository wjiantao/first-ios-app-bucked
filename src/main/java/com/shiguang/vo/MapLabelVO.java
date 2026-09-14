package com.shiguang.vo;

import lombok.Builder;
import lombok.Value;

/** 地图动态地点标签统一返回结构。 */
@Value
@Builder
public class MapLabelVO {
    String id;
    String name;
    String nameZh;
    String type;
    String address;
    Double longitude;
    Double latitude;
    Integer priority;
}
