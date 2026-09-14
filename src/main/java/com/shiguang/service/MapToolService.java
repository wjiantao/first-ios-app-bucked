package com.shiguang.service;

import com.shiguang.dto.MapElevationDTO;
import com.shiguang.vo.MapElevationVO;

import java.util.List;

/** 地图工具业务服务。 */
public interface MapToolService {
    List<MapElevationVO> elevation(MapElevationDTO request);
}
