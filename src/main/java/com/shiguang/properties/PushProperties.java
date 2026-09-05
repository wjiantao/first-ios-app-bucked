package com.shiguang.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 推送相关配置（application.yml 中 shiguang.push 前缀）。
 *
 * iOS 与 Android 统一走极光 JPush，后端只需要 appKey、masterSecret 和
 * APNs 生产环境开关。凭据通过 SHIGUANG_JPUSH_* 环境变量注入；
 * 未配置时 {@link #isConfigured()} 返回 false，推送服务降级为仅记日志，
 * 站内消息不受影响。
 */
@Component
@ConfigurationProperties(prefix = "shiguang.push")
@Data
public class PushProperties {

    private Jpush jpush = new Jpush();

    @Data
    public static class Jpush {
        /** 极光控制台分配的 AppKey */
        private String appKey;
        /** 极光控制台分配的 Master Secret */
        private String masterSecret;
        /**
         * iOS APNs 是否使用生产证书。
         * 本地调试默认 false，生产环境通过 SHIGUANG_JPUSH_APNS_PRODUCTION=true 覆盖。
         */
        private boolean apnsProduction = false;

        public boolean configured() {
            return appKey != null && !appKey.isBlank()
                    && masterSecret != null && !masterSecret.isBlank();
        }
    }

    /** 是否已配置 JPush 凭据。 */
    public boolean isConfigured() {
        return jpush.configured();
    }
}
