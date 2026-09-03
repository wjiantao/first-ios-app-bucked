package com.shiguang.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 邮箱注册第一步请求体：校验邮箱验证码。
 */
@Data
@Schema(description = "邮箱注册第一步：校验邮箱验证码")
public class RegisterVerifyDTO {

    @Schema(description = "邮箱地址", example = "demo@shiguang.app", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @Schema(description = "6 位数字验证码；开发模式下固定验证码 123456 也可通过", example = "123456",
            minLength = 6, maxLength = 6, requiredMode = Schema.RequiredMode.REQUIRED)
    private String code;
}
