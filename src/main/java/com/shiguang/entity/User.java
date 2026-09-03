package com.shiguang.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体，对应 users 表。
 *
 * 用户 ID 使用字符串（与 RN 端 LoginResult.user.id 的类型约定一致）。
 * email 用于账号密码登录，passwordHash 保存 PBKDF2 加盐摘要；
 * status 区分“pending（邮箱已验证、待设置密码）”和“active（可登录）”。
 */
@Data
public class User {

    private String id;
    private String email;
    private String passwordHash;
    private String nickname;
    private String status;
    private String bio;
    private String tagline;
    private String avatarUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
