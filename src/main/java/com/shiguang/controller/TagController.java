package com.shiguang.controller;

import com.shiguang.result.Result;
import com.shiguang.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 标签接口：查询已存在的标签名，供发布页话题联想。
 *
 * 候选即数据库已收录的标签（无热度等额外字段），
 * 只读公开数据，因此无需登录即可访问。
 */
@RestController
@RequestMapping(value = "/api/tags", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "标签接口", description = "查询已有标签")
@Slf4j
public class TagController {

    @Autowired
    private TagService tagService;

    /**
     * 查询已有标签名列表（去重，按名称升序）。
     *
     * 发布页话题联想使用：点选候选即可作为作品标签，也可自行新建。
     *
     * @return 已有标签名列表
     */
    @GetMapping
    @Operation(summary = "查询已有标签", description = "返回库中已存在的标签名（去重，按名称升序）")
    public Result<List<String>> list() {
        log.info("查询已有标签");
        return Result.success(tagService.list());
    }
}
