package com.shiguang.controller;

import com.shiguang.config.OpenApiConfiguration;
import com.shiguang.result.Result;
import com.shiguang.service.CreatorService;
import com.shiguang.vo.CreatorStatsVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 创作者接口（需登录）。
 *
 * 创作中心「数据概览」需要当前用户的浏览/点赞数据，因此接口要求 JWT，
 * 用户 ID 由拦截器写入 UserContext，控制器无需在参数里透传。
 */
@RestController
@RequestMapping(value = "/api/creator", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "创作者接口", description = "需携带 JWT 访问")
@Slf4j
@SecurityRequirement(name = OpenApiConfiguration.BEARER_AUTH)
public class CreatorController {

    @Autowired
    private CreatorService creatorService;

    /**
     * 数据概览（GET /api/creator/stats）。
     *
     * 返回当前登录用户的累计浏览量、今日新增浏览量、累计点赞（原始整数），
     * 供创作中心「数据概览」卡片展示，展示格式化由 RN 前端完成。
     */
    @Operation(summary = "创作者数据概览",
            description = "返回当前登录用户累计浏览量、今日新增浏览量、累计点赞；"
                    + "仅统计其已发布/下架作品，草稿与已删除不计入。")
    @GetMapping("/stats")
    public Result<CreatorStatsVO> stats() {
        return Result.success(creatorService.stats());
    }
}
