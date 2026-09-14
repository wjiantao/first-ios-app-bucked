package com.shiguang.provider;

import com.shiguang.dto.MapElevationDTO;
import com.shiguang.vo.MapElevationVO;

import java.util.List;

/** 高程 provider 抽象。 */
public interface ElevationProvider {
    List<MapElevationVO> elevation(MapElevationDTO request);
}
