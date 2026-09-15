package com.shiguang.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/** 创建评论请求；parentId 可指向任意层级的评论。 */
@Data
@Schema(description = "创建评论请求")
public class CommentCreateDTO {
    @Schema(description = "评论文字，和 imageUrl 至少提供一个")
    private String content;
    @Schema(description = "评论图片地址，最多一张", nullable = true)
    private String imageUrl;
    @Schema(description = "父评论 ID；为空表示顶级评论", nullable = true)
    private Long parentId;
}
