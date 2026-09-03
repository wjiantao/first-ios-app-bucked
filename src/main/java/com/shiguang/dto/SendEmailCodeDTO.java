package com.shiguang.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 发送邮箱验证码请求体。
 */
@Data
@Schema(description = "发送邮箱验证码请求")
public class SendEmailCodeDTO {

    @Schema(description = "邮箱地址", example = "demo@shiguang.app", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;
}
