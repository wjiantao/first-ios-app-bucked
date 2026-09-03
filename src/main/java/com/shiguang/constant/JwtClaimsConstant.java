package com.shiguang.constant;

/**
 * JWT 自定义声明字段名常量。
 *
 * 集中管理声明 key，避免签发与解析两侧手写字符串不一致。
 */
public class JwtClaimsConstant {

    public static final String USER_ID = "userId";

    private JwtClaimsConstant() {
    }
}
