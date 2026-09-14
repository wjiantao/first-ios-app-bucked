package com.shiguang.service.impl;

import com.shiguang.dto.LocationSearchDTO;
import com.shiguang.exception.BusinessException;
import com.shiguang.provider.LocationSearchProvider;
import com.shiguang.service.LocationSearchService;
import com.shiguang.vo.LocationSearchVO;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LocationSearchServiceImpl implements LocationSearchService {
    @Autowired private LocationSearchProvider provider;

    @Override
    public List<LocationSearchVO> search(LocationSearchDTO request) {
        if (request == null || request.getKeyword() == null || request.getKeyword().trim().length() < 2) {
            throw new BusinessException("地点关键词至少需要 2 个字符");
        }
        request.setKeyword(request.getKeyword().trim());
        request.setLimit(Math.max(1, Math.min(20, request.getLimit() == null ? 10 : request.getLimit())));
        return provider.search(request);
    }
}
