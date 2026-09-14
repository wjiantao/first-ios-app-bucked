package com.shiguang.service.impl;

import com.shiguang.dto.MapWorksQueryDTO;
import com.shiguang.exception.BusinessException;
import com.shiguang.mapper.MapWorkMapper;
import com.shiguang.service.MapService;
import com.shiguang.vo.MapWorkVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Cesium 地图作品查询服务实现。
 *
 * 地图视野通常是一个普通矩形；但当用户把地球转到 180° 经线附近时，
 * Cesium 地球视野跨越日期变更线时 west 可能大于 east。此时必须在服务端拆成
 * [west, 180] 和 [-180, east] 两段查询，再按发布时间合并，
 * 避免出现“东边界小于西边界导致查询为空”的问题。
 */
@Service
public class MapServiceImpl implements MapService {

    /** 地图接口默认返回条数。 */
    private static final int DEFAULT_LIMIT = 500;

    /** 地图接口单次最大返回条数，防止恶意超大 limit 拖垮 MySQL 和移动端。 */
    private static final int MAX_LIMIT = 1000;

    @Autowired
    private MapWorkMapper mapWorkMapper;

    @Override
    public List<MapWorkVO> listVisible(MapWorksQueryDTO query) {
        if (query == null) {
            throw new BusinessException("地图范围参数不能为空");
        }
        validateBounds(query);
        int limit = normalizeLimit(query.getLimit());

        // 正常矩形：west <= east，直接一次查询即可，保持 SQL 的时间倒序。
        if (query.getWest() <= query.getEast()) {
            return mapWorkMapper.selectVisible(
                    query.getWest(),
                    query.getEast(),
                    query.getSouth(),
                    query.getNorth(),
                    limit);
        }

        // 跨 180° 经线：分别查询 [west, 180] 与 [-180, east]，
        // 合并后重新按发布时间倒序，避免两段结果简单拼接破坏全局顺序。
        List<MapWorkVO> first = mapWorkMapper.selectVisible(
                query.getWest(),
                180.0,
                query.getSouth(),
                query.getNorth(),
                limit);
        List<MapWorkVO> second = mapWorkMapper.selectVisible(
                -180.0,
                query.getEast(),
                query.getSouth(),
                query.getNorth(),
                limit);
        return mergeAndLimit(first, second, limit);
    }

    /**
     * 校验地图边界参数。
     *
     * 四个边界必须同时存在，且纬度/经度都落在 WGS84 的合法数值范围内；
     * 同时南边界不能大于北边界，否则表示客户端传入了错误视野。
     */
    private void validateBounds(MapWorksQueryDTO query) {
        if (query.getWest() == null
                || query.getSouth() == null
                || query.getEast() == null
                || query.getNorth() == null) {
            throw new BusinessException("地图范围参数不完整");
        }
        if (!isFinite(query.getWest())
                || !isFinite(query.getEast())
                || !isFinite(query.getSouth())
                || !isFinite(query.getNorth())) {
            throw new BusinessException("地图范围参数必须是有限数字");
        }
        if (query.getSouth() > query.getNorth()) {
            throw new BusinessException("地图南边界不能大于北边界");
        }
        if (query.getSouth() < -90 || query.getSouth() > 90
                || query.getNorth() < -90 || query.getNorth() > 90) {
            throw new BusinessException("纬度超出合法范围(-90~90)");
        }
        if (query.getWest() < -180 || query.getWest() > 180
                || query.getEast() < -180 || query.getEast() > 180) {
            throw new BusinessException("经度超出合法范围(-180~180)");
        }
    }

    /** Double 能表达 NaN/Infinity，必须显式拦截，避免 SQL 收到不可比较的边界。 */
    private boolean isFinite(Double value) {
        return value != null && Double.isFinite(value);
    }

    /**
     * 裁剪 limit 到 [1, 1000]。
     *
     * limit 为 null 时使用默认值；小于 1 至少返回一条，
     * 大于 1000 则截断到上限，避免前端误传超大值。
     */
    private int normalizeLimit(Integer limit) {
        if (limit == null) {
            return DEFAULT_LIMIT;
        }
        return Math.max(1, Math.min(MAX_LIMIT, limit));
    }

    /**
     * 合并跨经线查询的两段结果。
     *
     * 使用 id 去重是因为作品若正好记录在 180/-180 边界附近，
     * 两段查询可能同时命中；按 sortTime 倒序合并后截取前 limit 条。
     */
    private List<MapWorkVO> mergeAndLimit(List<MapWorkVO> first,
                                          List<MapWorkVO> second,
                                          int limit) {
        Map<String, MapWorkVO> byId = new LinkedHashMap<>();
        for (MapWorkVO item : first) {
            byId.putIfAbsent(item.getId(), item);
        }
        for (MapWorkVO item : second) {
            byId.putIfAbsent(item.getId(), item);
        }

        List<MapWorkVO> merged = new ArrayList<>(byId.values());
        merged.sort(Comparator
                .comparing(MapWorkVO::getSortTime,
                        Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(MapWorkVO::getId));
        return merged.size() <= limit
                ? merged
                : new ArrayList<>(merged.subList(0, limit));
    }
}
