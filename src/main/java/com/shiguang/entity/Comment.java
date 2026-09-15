package com.shiguang.entity;

import lombok.Data;

import java.time.LocalDateTime;

/** 评论实体；parentId 自关联，支持任意深度回复。 */
@Data
public class Comment {
    private Long id;
    private String workId;
    private String authorId;
    private Long parentId;
    private String content;
    private String imageUrl;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime deletedAt;
}
