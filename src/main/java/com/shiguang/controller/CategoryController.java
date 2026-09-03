package com.shiguang.controller;

import com.shiguang.result.Result;
import com.shiguang.service.CategoryService;
import com.shiguang.vo.CategoryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 分类接口：查询作品分类。
 *
 * 分类列表是公开只读数据（首页游客可浏览、发布页共用），
 * 不包含用户私有信息，因此无需登录即可访问。
 */
@RestController
@RequestMapping(value = "/api/categories", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "分类接口", description = "查询作品分类")
@Slf4j
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 查询启用中的分类列表，按 sort_order 升序返回。
     *
     * @return 分类列表
     */
    @GetMapping
    @Operation(summary = "查询分类列表", description = "返回启用中的分类，按展示排序升序")
    public Result<List<CategoryVO>> list() {
        log.info("查询分类列表");
        return Result.success(categoryService.list());
    }
}
