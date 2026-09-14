package com.shiguang.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shiguang.dto.LocationSearchDTO;
import com.shiguang.exception.BusinessException;
import com.shiguang.vo.LocationSearchVO;
import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/** 兼容 Nominatim 风格响应的可配置 Provider；公共 Nominatim 不允许自动联想。 */
@Component
public class ConfiguredLocationSearchProvider implements LocationSearchProvider {
    private final RestTemplate restTemplate;
    private final ObjectMapper mapper;
    private final String url;
    private final String userAgent;

    public ConfiguredLocationSearchProvider(RestTemplateBuilder builder, ObjectMapper mapper,
            @Value("${shiguang.map.location-search-url:}") String url,
            @Value("${shiguang.map.user-agent:ShiguangMap/1.0}") String userAgent,
            @Value("${shiguang.map.provider-timeout-ms:8000}") long timeoutMs) {
        this.restTemplate = builder.setConnectTimeout(Duration.ofMillis(timeoutMs)).setReadTimeout(Duration.ofMillis(timeoutMs)).build();
        this.mapper = mapper;
        this.url = url;
        this.userAgent = userAgent == null || userAgent.isBlank() ? "ShiguangMap/1.0" : userAgent.trim();
    }

    @Override
    public List<LocationSearchVO> search(LocationSearchDTO request) {
        if (url == null || url.isBlank()) throw new BusinessException(503, "地点搜索服务未配置");
        String host = URI.create(url).getHost();
        if ("nominatim.openstreetmap.org".equalsIgnoreCase(host)) {
            throw new BusinessException(503, "公共 Nominatim 不允许自动联想，请配置自建或商业服务");
        }
        String target = UriComponentsBuilder.fromHttpUrl(url).queryParam("q", request.getKeyword()).queryParam("format", "jsonv2").queryParam("limit", request.getLimit()).queryParam("accept-language", "zh-CN").toUriString();
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            headers.set(HttpHeaders.USER_AGENT, userAgent);
            ResponseEntity<String> response = restTemplate.exchange(URI.create(target), HttpMethod.GET, new HttpEntity<>(headers), String.class);
            JsonNode root = mapper.readTree(response.getBody());
            List<LocationSearchVO> result = new ArrayList<>();
            if (!root.isArray()) return result;
            for (JsonNode item : root) {
                double latitude = item.path("lat").asDouble(Double.NaN);
                double longitude = item.path("lon").asDouble(Double.NaN);
                if (!Double.isFinite(latitude) || !Double.isFinite(longitude) || latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) continue;
                String name = item.path("display_name").asText(request.getKeyword());
                result.add(LocationSearchVO.builder().id(item.path("place_id").asText(item.path("osm_id").asText())).name(name).address(name).latitude(latitude).longitude(longitude).build());
            }
            return result;
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException(502, "地点搜索服务请求失败，请稍后重试");
        }
    }
}
