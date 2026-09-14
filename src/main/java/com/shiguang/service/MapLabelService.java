package com.shiguang.service;

import com.shiguang.dto.MapLabelsQueryDTO;
import com.shiguang.vo.MapLabelVO;
import java.util.List;

/** 查询可视范围内的动态地点名称。 */
public interface MapLabelService {
    List<MapLabelVO> listVisible(MapLabelsQueryDTO query);
}
