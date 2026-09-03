package com.shiguang.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT 相关配置（application.yml 中 shiguang.jwt 前缀）。
 */
@Component
@ConfigurationProperties(prefix = "shiguang.jwt")
@Data
public class JwtProperties {

    /** 签名密钥：生产环境必须通过环境变量注入，不能使用默认值 */
    private String secretKey;

    /** token 有效期（毫秒） */
    private long tokenTtl;
}
