package com.shiguang.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 第三方登录绑定关系，对应 auth_accounts 表。
 *
 * (channel, channelUid) 唯一：同一第三方账号只能绑定一个站内用户，
 * 避免重复登录时创建出多个账号。
 */
@Data
public class AuthAccount {

    private Long id;
    private String userId;
    private String channel;
    private String channelUid;
    private LocalDateTime createdAt;
}
