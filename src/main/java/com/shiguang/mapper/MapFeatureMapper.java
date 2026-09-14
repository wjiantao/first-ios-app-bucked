package com.shiguang.mapper;

import com.shiguang.entity.MapCheckin;
import com.shiguang.vo.MapCheckinVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/** 地图第二阶段能力的数据访问层。 */
public interface MapFeatureMapper {
    MapCheckinVO selectCheckin(@Param("userId") String userId, @Param("workId") String workId);

    int insertCheckin(MapCheckin checkin);

    List<MapCheckinVO> selectCheckins(@Param("userId") String userId);
}
