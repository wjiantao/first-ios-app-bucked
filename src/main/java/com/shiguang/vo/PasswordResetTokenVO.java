package com.shiguang.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

/** 邮箱验证码校验成功后返回的短期重置凭证。 */
@Data
@AllArgsConstructor
@Schema(description = "密码重置凭证")
public class PasswordResetTokenVO {

    @Schema(description = "一次性密码重置凭证")
    private String resetToken;
}
