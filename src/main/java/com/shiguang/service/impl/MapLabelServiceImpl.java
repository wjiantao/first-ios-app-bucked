package com.shiguang.service.impl;

import com.shiguang.dto.MapLabelsQueryDTO;
import com.shiguang.service.MapLabelService;
import com.shiguang.vo.MapLabelVO;
import com.shiguang.provider.MapLabelProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.List;

/**
 * 地点标签服务。
 *
 * Provider 尚未配置时返回空集合而不是让地图接口失败，保证底图和作品点位仍可用。
 */
@Service
public class MapLabelServiceImpl implements MapLabelService {
    @Autowired private MapLabelProvider provider;
    @Override
    public List<MapLabelVO> listVisible(MapLabelsQueryDTO query) {
        if (query == null || query.getWest() == null || query.getSouth() == null
                || query.getEast() == null || query.getNorth() == null) {
            return Collections.emptyList();
        }
        if (query.getSouth() < -90 || query.getNorth() > 90
                || query.getSouth() > query.getNorth()
                || query.getWest() < -180 || query.getWest() > 180
                || query.getEast() < -180 || query.getEast() > 180) {
            return Collections.emptyList();
        }
        query.setLimit(Math.max(1, Math.min(300, query.getLimit() == null ? 200 : query.getLimit())));
        query.setZoomLevel(Math.max(0, Math.min(22, query.getZoomLevel() == null ? 10 : query.getZoomLevel())));
        return provider.search(query);
    }
}
