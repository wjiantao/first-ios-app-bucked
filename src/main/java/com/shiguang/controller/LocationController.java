package com.shiguang.controller;

import com.shiguang.dto.LocationSearchDTO;
import com.shiguang.result.Result;
import com.shiguang.service.LocationSearchService;
import com.shiguang.vo.LocationSearchVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 发布作品的位置候选接口，与地图工具搜索保持独立。 */
@RestController
@RequestMapping(value = "/api/location", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "位置接口")
public class LocationController {
    @Autowired private LocationSearchService service;

    @Operation(summary = "搜索发布位置")
    @PostMapping("/search")
    public Result<List<LocationSearchVO>> search(@RequestBody LocationSearchDTO request) {
        return Result.success(service.search(request));
    }
}
