package com.shiguang.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 个人资料页分页查询请求（GET /api/users/me/works|favorites|likes 共用）。
 *
 * 不需要 userId：当前登录用户从 JWT 上下文取；
 * current/pageSize 缺省为 1/10，与 WorkPageQueryDTO 的分页约定保持一致。
 */
@Data
@Schema(description = "个人资料页分页查询请求（我的作品 / 收藏 / 喜欢）")
public class ProfilePageQueryDTO {

    /** 页码，从 1 开始，缺省 1。 */
    @Schema(description = "页码，从 1 开始", example = "1", defaultValue = "1")
    private Integer current = 1;

    /** 每页条数，缺省 10。 */
    @Schema(description = "每页条数", example = "10", defaultValue = "10")
    private Integer pageSize = 10;
}
