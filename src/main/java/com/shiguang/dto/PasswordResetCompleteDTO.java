package com.shiguang.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/** 密码重置完成请求体。 */
@Data
@Schema(description = "密码重置完成请求")
public class PasswordResetCompleteDTO {

    @Schema(description = "邮箱验证码校验后签发的一次性重置凭证", requiredMode = Schema.RequiredMode.REQUIRED)
    private String resetToken;

    @Schema(description = "新登录密码，至少 6 位", example = "secret123",
            minLength = 6, requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;
}
