package com.shiguang.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 邮箱 + 密码登录请求体。
 */
@Data
@Schema(description = "邮箱 + 密码登录请求")
public class EmailLoginDTO {

    @Schema(description = "邮箱地址", example = "demo@shiguang.app", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @Schema(description = "登录密码", example = "secret123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;
}
