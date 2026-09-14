package com.shiguang.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/** 地图打卡记录展示对象。 */
@Data
@Schema(description = "地图打卡记录")
public class MapCheckinVO {
    private String workId;
    private String title;
    private String locationName;
    private LocalDateTime checkedInAt;
}
