package com.shiguang.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 第三方登录请求体。
 *
 * 苹果登录传 identityToken，微信/抖音传 code；
 * 开发模式下二者都不必是真实凭据，任意非空字符串即可（见 AuthServiceImpl）。
 */
@Data
@Schema(description = "第三方登录请求体：苹果登录传 identityToken，微信/抖音传 code")
public class SocialLoginDTO {

    @Schema(description = "微信/抖音授权 code（开发模式下可为任意非空字符串）", example = "wx-auth-code")
    private String code;

    @Schema(description = "Apple 授权 identityToken（开发模式下可为任意非空字符串）", example = "apple-identity-token")
    private String identityToken;
}
