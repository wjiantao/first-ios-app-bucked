package com.shiguang.mapper;

import com.shiguang.vo.MapWorkVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Cesium 地图作品查询 Mapper。
 *
 * SQL 收敛在 resources/mapper/MapWorkMapper.xml 中，只查询已发布且带坐标的作品。
 */
public interface MapWorkMapper {

    /**
     * 查询可视矩形内的已发布作品标记。
     *
     * @param west  西边界经度
     * @param east  东边界经度
     * @param south 南边界纬度
     * @param north 北边界纬度
     * @param limit 最多返回条数
     * @return 按发布时间倒序的作品标记列表
     */
    List<MapWorkVO> selectVisible(@Param("west") Double west,
                                  @Param("east") Double east,
                                  @Param("south") Double south,
                                  @Param("north") Double north,
                                  @Param("limit") int limit);
}
