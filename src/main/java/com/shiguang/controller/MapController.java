package com.shiguang.controller;

import com.shiguang.dto.MapWorksQueryDTO;
import com.shiguang.dto.MapLabelsQueryDTO;
import com.shiguang.dto.MapCheckinDTO;
import com.shiguang.dto.MapElevationDTO;
import com.shiguang.context.UserContext;
import com.shiguang.service.MapFeatureService;
import com.shiguang.service.MapToolService;
import com.shiguang.vo.MapCheckinVO;
import com.shiguang.result.Result;
import com.shiguang.service.MapService;
import com.shiguang.service.MapLabelService;
import com.shiguang.vo.MapWorkVO;
import com.shiguang.vo.MapElevationVO;
import com.shiguang.vo.MapLabelVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 地图浏览与地图互动接口。
 *
 * 作品点位允许游客公开浏览；地图打卡接口仍由 WebMvcConfiguration 纳入 JWT
 * 拦截范围，避免把“浏览公开”误解成所有地图写操作都不需要身份。
 */
@RestController
@RequestMapping(value = "/api/map", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "地图接口", description = "公开接口，返回可视范围内的已发布作品标记")
public class MapController {

    @Autowired
    private MapService mapService;

    @Autowired
    private MapLabelService mapLabelService;

    @Autowired
    private MapFeatureService mapFeatureService;

    @Autowired
    private MapToolService mapToolService;

    /**
     * 查询当前 Cesium 地图可视范围内的作品。
     *
     * @param query west/south/east/north 可视矩形与 limit
     * @return 该范围内的作品标记列表
     */
    @Operation(summary = "查询可视范围作品",
            description = "按 Cesium 相机可视矩形返回 WGS84 坐标的已发布作品标记，"
                    + "无需登录；limit 默认 500，最大 1000。")
    @PostMapping("/works")
    public Result<List<MapWorkVO>> works(@RequestBody MapWorksQueryDTO query) {
        return Result.success(mapService.listVisible(query));
    }

    /** 查询当前视野内、按缩放级别裁剪的中文地点标签。 */
    @Operation(summary = "查询地图地点名称")
    @PostMapping("/labels")
    public Result<List<MapLabelVO>> labels(@RequestBody MapLabelsQueryDTO query) {
        return Result.success(mapLabelService.listVisible(query));
    }

    /** 批量查询地形高程。 */
    @Operation(summary = "查询地图高程")
    @PostMapping("/elevation")
    public Result<List<MapElevationVO>> elevation(@RequestBody MapElevationDTO request) {
        return Result.success(mapToolService.elevation(request));
    }

    /** 保存当前登录用户的地图作品打卡，重复打卡返回原记录。 */
    @Operation(summary = "地图作品打卡", description = "需要登录；按用户和作品去重，坐标使用 GCJ-02")
    @PostMapping("/checkins")
    public Result<MapCheckinVO> checkIn(@RequestBody MapCheckinDTO request) {
        return Result.success(mapFeatureService.checkIn(UserContext.getCurrentId(), request));
    }

    /** 查询当前登录用户的打卡记录。 */
    @Operation(summary = "我的地图打卡")
    @GetMapping("/checkins")
    public Result<List<MapCheckinVO>> checkins() {
        return Result.success(mapFeatureService.listCheckins(UserContext.getCurrentId()));
    }
}
