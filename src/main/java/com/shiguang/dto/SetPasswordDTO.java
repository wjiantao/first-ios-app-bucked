package com.shiguang.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 邮箱注册第二步请求体：验证邮箱通过后设置密码（可选昵称）。
 */
@Data
@Schema(description = "邮箱注册第二步：设置密码并激活账号")
public class SetPasswordDTO {

    @Schema(description = "邮箱地址（须先通过 email-verify 校验）", example = "demo@shiguang.app",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @Schema(description = "登录密码，至少 6 位（服务端以 PBKDF2 加盐摘要存储）", example = "secret123",
            minLength = 6, requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

    @Schema(description = "昵称，可选；为空时服务端自动取邮箱前缀", example = "拾光")
    private String nickname;
}
