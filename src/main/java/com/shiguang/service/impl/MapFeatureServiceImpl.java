package com.shiguang.service.impl;

import com.shiguang.dto.MapCheckinDTO;
import com.shiguang.entity.MapCheckin;
import com.shiguang.exception.BusinessException;
import com.shiguang.mapper.MapFeatureMapper;
import com.shiguang.service.MapFeatureService;
import com.shiguang.vo.MapCheckinVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/** 地图打卡服务实现，服务端负责身份校验和重复打卡去重。 */
@Service
public class MapFeatureServiceImpl implements MapFeatureService {

    @Autowired
    private MapFeatureMapper mapFeatureMapper;

    @Override
    @Transactional
    public MapCheckinVO checkIn(String userId, MapCheckinDTO request) {
        if (userId == null || userId.isBlank()) {
            throw new BusinessException("请先登录后再打卡");
        }
        validate(request);
        MapCheckinVO existing = mapFeatureMapper.selectCheckin(userId, request.getWorkId());
        if (existing != null) {
            return existing;
        }
        MapCheckin record = new MapCheckin();
        record.setUserId(userId);
        record.setWorkId(request.getWorkId());
        record.setLatitude(request.getLatitude());
        record.setLongitude(request.getLongitude());
        record.setLocationName(request.getLocationName());
        record.setCreatedAt(LocalDateTime.now());
        mapFeatureMapper.insertCheckin(record);
        return mapFeatureMapper.selectCheckin(userId, request.getWorkId());
    }

    @Override
    public List<MapCheckinVO> listCheckins(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new BusinessException("请先登录后查看打卡记录");
        }
        return mapFeatureMapper.selectCheckins(userId);
    }

    private void validate(MapCheckinDTO request) {
        if (request == null || request.getWorkId() == null || request.getWorkId().isBlank()) {
            throw new BusinessException("打卡作品不能为空");
        }
        if (!finite(request.getLatitude()) || !finite(request.getLongitude())
                || request.getLatitude() < -90 || request.getLatitude() > 90
                || request.getLongitude() < -180 || request.getLongitude() > 180) {
            throw new BusinessException("打卡坐标不合法");
        }
    }

    private boolean finite(Double value) {
        return value != null && Double.isFinite(value);
    }
}
