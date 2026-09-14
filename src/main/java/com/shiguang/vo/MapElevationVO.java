package com.shiguang.vo;

import lombok.Builder;
import lombok.Value;

/** 统一的高程结果。 */
@Value
@Builder
public class MapElevationVO {
    Double longitude;
    Double latitude;
    Double elevation;
    String source;
}
