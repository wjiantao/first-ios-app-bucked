package com.shiguang.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 推送设备注册请求体（POST /api/push/devices）。
 */
@Data
@Schema(description = "推送设备注册请求")
public class PushDeviceRegisterDTO {

    @Schema(description = "设备平台：ios / android", example = "ios")
    private String platform;

    @Schema(description = "推送厂商：当前仅支持 jpush", example = "jpush")
    private String vendor;

    @Schema(description = "推送 token（JPush registrationID）", example = "xxxxxxxx")
    private String token;
}
