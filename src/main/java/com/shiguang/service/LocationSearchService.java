package com.shiguang.service;

import com.shiguang.dto.LocationSearchDTO;
import com.shiguang.vo.LocationSearchVO;
import java.util.List;

public interface LocationSearchService {
    List<LocationSearchVO> search(LocationSearchDTO request);
}
