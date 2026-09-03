package com.shiguang.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 登录成功返回：token + 当前用户。
 *
 * 字段结构与 RN 端 LoginResult 约定保持一致：
 * { token, user: { id, nickname } }，便于客户端后续直接替换 Model。
 */
@Data
@Builder
@Schema(description = "登录成功返回：JWT token + 当前用户信息")
public class LoginVO {

    @Schema(description = "JWT 令牌；后续请求放入请求头 Authorization: Bearer <token>", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String token;

    @Schema(description = "当前登录用户信息")
    private UserInfoVO user;
}
