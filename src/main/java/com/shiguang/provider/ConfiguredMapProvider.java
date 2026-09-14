package com.shiguang.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shiguang.dto.MapElevationDTO;
import com.shiguang.exception.BusinessException;
import com.shiguang.vo.MapElevationVO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 默认开放协议 provider。
 *
 * 高程 provider 可以通过环境变量替换为兼容服务，因此客户端不需要因为供应商更换而改动。
 */
@Component
public class ConfiguredMapProvider implements ElevationProvider {
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String elevationUrl;
    private final String userAgent;

    public ConfiguredMapProvider(
            RestTemplateBuilder builder,
            ObjectMapper objectMapper,
            @Value("${shiguang.map.elevation-url:}") String elevationUrl,
            @Value("${shiguang.map.user-agent:ShiguangMap/1.0}") String userAgent,
            @Value("${shiguang.map.provider-timeout-ms:8000}") long timeoutMs) {
        this.restTemplate = builder
                .setConnectTimeout(Duration.ofMillis(timeoutMs))
                .setReadTimeout(Duration.ofMillis(timeoutMs))
                .build();
        this.objectMapper = objectMapper;
        this.elevationUrl = elevationUrl;
        this.userAgent = userAgent == null || userAgent.isBlank() ? "ShiguangMap/1.0" : userAgent.trim();
    }

    @Override
    public List<MapElevationVO> elevation(MapElevationDTO request) {
        requireConfigured(elevationUrl, "高程服务未配置");
        String locations = request.getPoints().stream().map(point -> point.getLatitude() + "," + point.getLongitude()).reduce((a, b) -> a + "|" + b).orElse("");
        JsonNode root = getJson(UriComponentsBuilder.fromHttpUrl(elevationUrl).queryParam("locations", locations).toUriString());
        List<MapElevationVO> result = new ArrayList<>();
        JsonNode values = root.path("results");
        for (int index = 0; index < values.size() && index < request.getPoints().size(); index++) {
            MapElevationDTO.Point point = request.getPoints().get(index);
            result.add(MapElevationVO.builder().longitude(point.getLongitude()).latitude(point.getLatitude()).elevation(values.get(index).path("elevation").asDouble()).source("configured-provider").build());
        }
        return result;
    }

    private JsonNode getJson(String url) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            headers.set(HttpHeaders.USER_AGENT, userAgent);
            ResponseEntity<String> response = restTemplate.exchange(
                    URI.create(url), HttpMethod.GET, new HttpEntity<>(headers), String.class);
            return objectMapper.readTree(response.getBody());
        } catch (Exception ex) {
            throw new BusinessException(502, "地图服务请求失败，请稍后重试");
        }
    }

    private void requireConfigured(String url, String message) {
        if (url == null || url.isBlank()) throw new BusinessException(503, message);
    }

    private boolean valid(double latitude, double longitude) {
        return Double.isFinite(latitude) && Double.isFinite(longitude) && latitude >= -90 && latitude <= 90 && longitude >= -180 && longitude <= 180;
    }

}
