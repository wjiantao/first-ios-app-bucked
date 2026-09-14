package com.shiguang.service;

import com.shiguang.dto.MapCheckinDTO;
import com.shiguang.vo.MapCheckinVO;

import java.util.List;

/** 地图打卡等需要用户身份的扩展服务。 */
public interface MapFeatureService {
    MapCheckinVO checkIn(String userId, MapCheckinDTO request);

    List<MapCheckinVO> listCheckins(String userId);
}
