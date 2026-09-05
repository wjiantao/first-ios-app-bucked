package com.shiguang.controller;

import com.shiguang.config.OpenApiConfiguration;
import com.shiguang.dto.PushDeviceRegisterDTO;
import com.shiguang.result.Result;
import com.shiguang.service.PushDeviceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

/**
 * 推送设备接口（需登录）。
 *
 * App 在拿到设备推送 token 后调用注册，从而使作者在收到点赞/收藏时收到系统推送。
 */
@RestController
@RequestMapping(value = "/api/push/devices", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "推送设备接口", description = "需携带 JWT 访问")
@Slf4j
@SecurityRequirement(name = OpenApiConfiguration.BEARER_AUTH)
public class PushDeviceController {

    @Autowired
    private PushDeviceService pushDeviceService;

    @Operation(summary = "注册/更新推送设备",
            description = "注册当前用户某平台某厂商的 JPush registrationID；同平台同厂商重复注册会覆盖旧 token。")
    @PostMapping
    public Result<Void> register(@RequestBody PushDeviceRegisterDTO dto) {
        log.info("推送设备注册：platform={} vendor={}", dto.getPlatform(), dto.getVendor());
        pushDeviceService.register(dto);
        return Result.success();
    }

    @Operation(summary = "注销推送设备",
            description = "注销当前用户某平台某厂商的推送设备（如退出登录或关闭推送）。")
    @DeleteMapping("/{platform}/{vendor}")
    public Result<Void> unregister(@PathVariable String platform,
                                   @PathVariable String vendor) {
        log.info("推送设备注销：platform={} vendor={}", platform, vendor);
        pushDeviceService.unregister(platform, vendor);
        return Result.success();
    }
}
