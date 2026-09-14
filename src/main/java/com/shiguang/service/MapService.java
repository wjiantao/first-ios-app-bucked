package com.shiguang.service;

import com.shiguang.dto.MapWorksQueryDTO;
import com.shiguang.vo.MapWorkVO;

import java.util.List;

/**
 * Cesium 地图作品查询服务。
 */
public interface MapService {

    /**
     * 查询可视范围内的已发布作品。
     *
     * @param query 可视范围查询参数
     * @return 按发布时间倒序的作品标记列表
     */
    List<MapWorkVO> listVisible(MapWorksQueryDTO query);
}
