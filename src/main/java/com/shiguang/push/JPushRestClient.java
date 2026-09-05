package com.shiguang.push;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shiguang.properties.PushProperties;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 极光 JPush REST API 客户端。
 *
 * 只封装本业务需要的单设备通知推送，使用 JDK HttpClient，避免引入官方 SDK。
 * JPush v3 push 接口要求 Basic Auth 使用 appKey:masterSecret。
 */
@Component
public class JPushRestClient {

    private static final String JPUSH_PUSH_URL = "https://api.jpush.cn/v3/push";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final PushProperties pushProperties;
    private final HttpClient httpClient;

    public JPushRestClient(PushProperties pushProperties) {
        this.pushProperties = pushProperties;
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    /**
     * 给单个 registrationID 发送通知。
     *
     * @param registrationId 客户端注册的 JPush registrationID
     * @param title          通知标题
     * @param body           通知正文
     * @param extras         附加字段，用于客户端跳转详情和标记已读
     * @param badge          iOS 角标数
     * @throws Exception JPush 未配置、网络异常或服务端返回非 200 时抛出
     */
    public void sendToRegistrationId(String registrationId,
                                     String title,
                                     String body,
                                     Map<String, String> extras,
                                     long badge) throws Exception {
        PushProperties.Jpush jpush = pushProperties.getJpush();
        if (!pushProperties.isConfigured()) {
            throw new IllegalStateException("JPush 未配置");
        }

        Map<String, Object> root = new LinkedHashMap<>();
        root.put("platform", "all");

        Map<String, Object> audience = new LinkedHashMap<>();
        audience.put("registration_id", List.of(registrationId));
        root.put("audience", audience);

        Map<String, Object> notification = new LinkedHashMap<>();
        notification.put("alert", body);

        Map<String, Object> iosAlert = new LinkedHashMap<>();
        iosAlert.put("title", title);
        iosAlert.put("body", body);
        Map<String, Object> ios = new LinkedHashMap<>();
        ios.put("alert", iosAlert);
        ios.put("sound", "default");
        ios.put("badge", badge);
        ios.put("extras", extras);
        notification.put("ios", ios);

        Map<String, Object> android = new LinkedHashMap<>();
        android.put("alert", title);
        android.put("title", title);
        android.put("extras", extras);
        notification.put("android", android);
        root.put("notification", notification);

        Map<String, Object> options = new LinkedHashMap<>();
        options.put("apns_production", jpush.isApnsProduction());
        options.put("time_to_live", 86400);
        root.put("options", options);

        String auth = Base64.getEncoder().encodeToString(
                (jpush.getAppKey() + ":" + jpush.getMasterSecret())
                        .getBytes(StandardCharsets.UTF_8));

        HttpRequest request = HttpRequest.newBuilder(URI.create(JPUSH_PUSH_URL))
                .header("Authorization", "Basic " + auth)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(
                        OBJECT_MAPPER.writeValueAsString(root)))
                .build();

        HttpResponse<String> response = httpClient.send(
                request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IllegalStateException(
                    "JPush 返回 " + response.statusCode() + ": " + response.body());
        }
    }
}
