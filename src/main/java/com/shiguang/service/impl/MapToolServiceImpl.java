package com.shiguang.service.impl;

import com.shiguang.dto.MapElevationDTO;
import com.shiguang.exception.BusinessException;
import com.shiguang.provider.ElevationProvider;
import com.shiguang.service.MapToolService;
import com.shiguang.vo.MapElevationVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 地图工具服务实现。
 *
 * 这里只负责统一校验和调用 provider，不把第三方返回结构泄漏到 Controller，
 * 后续替换 Nominatim/OSRM/OpenTopoData 时客户端契约保持稳定。
 */
@Service
public class MapToolServiceImpl implements MapToolService {
    @Autowired private ElevationProvider elevationProvider;

    @Override
    public List<MapElevationVO> elevation(MapElevationDTO request) {
        if (request == null || request.getPoints() == null || request.getPoints().isEmpty() || request.getPoints().size() > 50) {
            throw new BusinessException("高程查询点数量必须在 1 到 50 之间");
        }
        request.getPoints().forEach(this::validatePoint);
        return elevationProvider.elevation(request);
    }

    private void validatePoint(MapElevationDTO.Point point) {
        if (point == null || !valid(point.getLatitude(), point.getLongitude())) throw new BusinessException("坐标超出 WGS84 合法范围");
    }

    private boolean valid(Double latitude, Double longitude) {
        return latitude != null && longitude != null && Double.isFinite(latitude) && Double.isFinite(longitude) && latitude >= -90 && latitude <= 90 && longitude >= -180 && longitude <= 180;
    }
}
