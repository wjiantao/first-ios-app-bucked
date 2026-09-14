package com.shiguang.provider;

import com.shiguang.dto.MapLabelsQueryDTO;
import com.shiguang.vo.MapLabelVO;
import java.util.List;

/** 地图动态地点名称 Provider。 */
public interface MapLabelProvider {
    List<MapLabelVO> search(MapLabelsQueryDTO query);
}
