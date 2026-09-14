package com.shiguang.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shiguang.dto.MapLabelsQueryDTO;
import com.shiguang.exception.BusinessException;
import com.shiguang.vo.MapLabelVO;
import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/** 可配置地点标签 Provider；不允许误用公共 Nominatim 服务。 */
@Component
public class ConfiguredMapLabelProvider implements MapLabelProvider {
    private final RestTemplate restTemplate;
    private final ObjectMapper mapper;
    private final String url;
    private final String userAgent;

    public ConfiguredMapLabelProvider(RestTemplateBuilder builder, ObjectMapper mapper,
            @Value("${shiguang.map.labels-url:}") String url,
            @Value("${shiguang.map.user-agent:ShiguangMap/1.0}") String userAgent,
            @Value("${shiguang.map.provider-timeout-ms:8000}") long timeoutMs) {
        this.restTemplate = builder.setConnectTimeout(Duration.ofMillis(timeoutMs)).setReadTimeout(Duration.ofMillis(timeoutMs)).build();
        this.mapper = mapper;
        this.url = url;
        this.userAgent = userAgent == null || userAgent.isBlank() ? "ShiguangMap/1.0" : userAgent.trim();
    }

    @Override
    public List<MapLabelVO> search(MapLabelsQueryDTO query) {
        if (url == null || url.isBlank()) return Collections.emptyList();
        String host = URI.create(url).getHost();
        if ("nominatim.openstreetmap.org".equalsIgnoreCase(host)) {
            throw new BusinessException(503, "公共 Nominatim 不允许作为地点标签服务");
        }
        String target = UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("west", query.getWest()).queryParam("south", query.getSouth())
                .queryParam("east", query.getEast()).queryParam("north", query.getNorth())
                .queryParam("zoomLevel", query.getZoomLevel()).queryParam("limit", query.getLimit()).toUriString();
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            headers.set(HttpHeaders.USER_AGENT, userAgent);
            ResponseEntity<String> response = restTemplate.exchange(URI.create(target), HttpMethod.GET, new HttpEntity<>(headers), String.class);
            JsonNode root = mapper.readTree(response.getBody());
            if (!root.isArray()) return Collections.emptyList();
            List<MapLabelVO> result = new ArrayList<>();
            for (JsonNode item : root) {
                double latitude = item.path("latitude").asDouble(item.path("lat").asDouble(Double.NaN));
                double longitude = item.path("longitude").asDouble(item.path("lon").asDouble(Double.NaN));
                String name = item.path("nameZh").asText(item.path("name").asText("")).trim();
                if (name.isEmpty() || !Double.isFinite(latitude) || !Double.isFinite(longitude) || latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) continue;
                String type = item.path("type").asText("poi");
                if (!List.of("city", "district", "road", "poi", "building").contains(type)) type = "poi";
                result.add(MapLabelVO.builder().id(item.path("id").asText(name)).name(name).nameZh(item.path("nameZh").asText(null)).type(type).address(item.path("address").asText(null)).longitude(longitude).latitude(latitude).priority(item.path("priority").asInt(0)).build());
            }
            return result;
        } catch (BusinessException ex) { throw ex; }
        catch (Exception ex) { throw new BusinessException(502, "地点名称服务请求失败，请稍后重试"); }
    }
}
