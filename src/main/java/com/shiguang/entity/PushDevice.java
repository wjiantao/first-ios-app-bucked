package com.shiguang.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 推送设备实体，对应 push_devices 表。
 *
 * platform 取值 ios/android；vendor 当前仅支持 jpush。
 * 同一用户同一平台同一 vendor 仅保留一个 token，重复注册会覆盖旧 token。
 * iOS/Android 统一使用 JPush registrationID。
 */
@Data
public class PushDevice {

    private Long id;
    private String userId;
    private String platform;
    private String vendor;
    private String token;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
