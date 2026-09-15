package com.shiguang.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** 平铺评论展示对象；通过 parentId 和 replyToNickname 表达回复关系。 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentVO {
    private Long id;
    private String workId;
    private String authorId;
    private String authorNickname;
    private String authorAvatarUrl;
    private Long parentId;
    private String replyToAuthorId;
    private String replyToNickname;
    private String content;
    private String imageUrl;
    private boolean mine;
    private boolean deleted;
    private LocalDateTime createdAt;
}
