package com.shiguang.vo;

import io.swagger.v3.oas.annotations.media.Schema;

public class UpdatePassword {
    @Schema(description = "JWT 令牌；后续请求放入请求头 Authorization: Bearer <token>", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String token;

    @Schema(description = "当前登录用户信息")
    private UserInfoVO user;
}
