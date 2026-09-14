package com.shiguang.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 用户信息视图对象（登录返回与 /me 共用）。
 */
@Data
@Builder
@Schema(description = "用户信息（登录返回与 /me 共用）")
public class UserInfoVO {

    @Schema(description = "用户 ID", example = "u-1a2b3c4d5e6f")
    private String id;

    @Schema(description = "昵称", example = "拾光")
    private String nickname;

    @Schema(description = "头像 URL（/uploads/... 相对路径或完整 URL）", example = "/uploads/3f9c2a1b4d5e6f7a8b9c0d1e2f3a4b5c.jpg", nullable = true)
    private String avatarUrl;

    @Schema(description = "个人简介", example = "记录生活里的光")
    private String bio;

    @Schema(description = "个性签名", example = "保持热爱")
    private String tagline;

    @Schema(description = "粉丝数", example = "12")
    private long followerCount;
}
