package com.shiguang.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 修改用户资料请求体。
 *
 * 字段与 UserMapper.update 的局部更新语义一致：
 * 传 null 的字段保持不变，只更新传入的非空字段（昵称/简介/头像/签名）。
 * email、status 等不允许客户端修改，故不出现在请求体中。
 */
@Data
@Schema(description = "修改当前用户资料请求：传 null 的字段保持不变，只更新传入的非空字段")
public class UpdateUserDTO {

    @Schema(description = "昵称", example = "拾光")
    private String nickname;

    @Schema(description = "个人简介", example = "记录生活里的光")
    private String bio;

    @Schema(description = "头像 URL（建议使用通用上传接口返回的 /uploads/... 路径）", example = "/uploads/3f9c2a1b4d5e6f7a8b9c0d1e2f3a4b5c.jpg")
    private String avatarUrl;

    @Schema(description = "个性签名", example = "保持热爱")
    private String tagline;
}
