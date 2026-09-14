package com.shiguang.provider;

import com.shiguang.dto.LocationSearchDTO;
import com.shiguang.vo.LocationSearchVO;
import java.util.List;

/** 可配置的地点搜索服务抽象。 */
public interface LocationSearchProvider {
    List<LocationSearchVO> search(LocationSearchDTO request);
}
