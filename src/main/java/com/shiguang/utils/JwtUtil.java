package com.shiguang.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

/**
 * JWT 工具类：签发与解析。
 *
 * 使用 HS256 对称签名。密钥保存在服务端，客户端无法伪造 token；
 * 该实现与 sky-take-out 保持一致，方便后续按同一套代码理解。
 */
public class JwtUtil {

    private JwtUtil() {
    }

    public static String createJWT(String secretKey, long ttlMillis, Map<String, Object> claims) {
        long expMillis = System.currentTimeMillis() + ttlMillis;

        JwtBuilder builder = Jwts.builder()
                // 自定义声明必须先于标准声明设置，避免覆盖标准字段
                .setClaims(claims)
                .signWith(SignatureAlgorithm.HS256, secretKey.getBytes(StandardCharsets.UTF_8))
                .setExpiration(new Date(expMillis));

        return builder.compact();
    }

    public static Claims parseJWT(String secretKey, String token) {
        return Jwts.parser()
                .setSigningKey(secretKey.getBytes(StandardCharsets.UTF_8))
                .parseClaimsJws(token)
                .getBody();
    }
}
