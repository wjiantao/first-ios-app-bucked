package com.shiguang.controller;

import com.shiguang.result.Result;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 健康检查接口，供 Render 等服务平台的健康探针使用。
 *
 * 该接口不依赖数据库/Redis，只要进程存活即返回 200，便于 Render 判断实例是否就绪。
 */
@RestController
public class HealthController {

    @GetMapping(value = "/health", produces = MediaType.APPLICATION_JSON_VALUE)
    public Result<String> health() {
        return Result.success("ok");
    }
}
