package com.shiguang.controller;

import com.shiguang.config.OpenApiConfiguration;
import com.shiguang.dto.NearbyWorksQueryDTO;
import com.shiguang.dto.WorkPageQueryDTO;
import com.shiguang.dto.WorkPublishDTO;
import com.shiguang.dto.WorkUpdateDTO;
import com.shiguang.result.PageResult;
import com.shiguang.result.Result;
import com.shiguang.service.WorkService;
import com.shiguang.vo.FavoriteResultVO;
import com.shiguang.vo.WorkLikeResultVO;
import com.shiguang.vo.WorkVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

/**
 * 帖子接口。
 *
 * 作品分页列表对游客公开（首页瀑布流无需登录即可浏览）；
 * 发布作品与查看单条详情仍要求登录：
 * 详情接口可能返回草稿正文，不能暴露给未登录用户。
 */
@RestController
@RequestMapping(value = "/api/works", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "帖子接口", description = "作品列表公开；发布与详情需携带 JWT 访问")
@Slf4j
public class WorkController {

    @Autowired
    private WorkService workService;

    /**
     * 分页查询作品列表（公开接口，无需登录）。
     *
     * 首页瀑布流在未登录状态下也要能正常浏览，
     * 因此该接口不要求 JWT；published 过滤由客户端查询参数保证，
     * 服务端同样只返回未删除的作品。
     *
     * @param workPageQueryDTO
     * @return
     */
    @Operation(summary = "分页查询作品列表",
            description = "按 current/pageSize 分页，返回 {total, records}。"
                    + "支持 status/keyword 筛选：keyword 按标题模糊匹配；deleted 状态默认不返回。"
                    + "排序：已发布按发布时间倒序，草稿退化为创建时间。")
    @PostMapping("/page")
    public Result<PageResult<WorkVO>> page(@RequestBody WorkPageQueryDTO workPageQueryDTO) {
        log.info("分页查询帖子：{}", workPageQueryDTO);
        PageResult<WorkVO> pageResult = workService.pageQuery(workPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 查询附近的作品（公开接口，无需登录，与分页列表一致）。
     *
     * @param nearbyWorksQueryDTO 中心经纬度/半径/分页参数
     * @return 按距离由近到远排序的作品分页
     */
    @Operation(summary = "查询附近的作品",
            description = "以 center 经纬度为圆心、radiusKm 为半径（公里），返回已发布且带坐标的作品，"
                    + "按与圆心的距离升序；字段 distance 为与中心的距离（公里）。公开接口，无需登录。")
    @PostMapping("/nearby")
    public Result<PageResult<WorkVO>> nearby(@RequestBody NearbyWorksQueryDTO nearbyWorksQueryDTO) {
        log.info("查询附近作品：{}", nearbyWorksQueryDTO);
        PageResult<WorkVO> pageResult = workService.pageNearby(nearbyWorksQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 发布/保存作品草稿（需登录）。
     *
     * @param workPublishDTO
     * @return
     */
    @Operation(summary = "发布作品",
            description = "提交 status/title/coverUrl/videoUrl/contentMd/categoryIds；"
                    + "服务端生成作品 ID 并把 categoryIds 写入 work_categories 关联表，"
                    + "返回创建完成的作品（含所属分类）。status=draft 保存草稿、published 发布。")
    @SecurityRequirement(name = OpenApiConfiguration.BEARER_AUTH)
    @PostMapping
    public Result<WorkVO> insert(@RequestBody WorkPublishDTO workPublishDTO) {
        log.info("发布作品：{}", workPublishDTO);
        WorkVO result = workService.insert(workPublishDTO);
        return Result.success(result);
    }

    /**
     * 查看单条作品（需登录）。
     *
     * 该接口与发布共用 getById，可能返回草稿/未发布内容，
     * 因此不能像列表一样对游客开放，避免草稿被未登录请求遍历到。
     *
     * @param id
     * @return
     */
    @Operation(summary = "查看作品")
    @SecurityRequirement(name = OpenApiConfiguration.BEARER_AUTH)
    @GetMapping("/{id}")
    public Result<WorkVO> getById(@PathVariable String id) {
        log.info("查看作品：{}", id);
        WorkVO result = workService.getById(id);
        return Result.success(result);
    }

    /**
     * 修改作品（需登录，仅作者可改）。
     *
     * 部分更新语义：标量字段 null=保持不变、列表字段 null=保持不变、[]=清空。
     *
     * @param id           作品 ID
     * @param workUpdateDTO 修改请求体
     * @return 修改完成的作品（含所属分类与标签）
     */
    @Operation(summary = "修改作品",
            description = "仅作者可改；status/title/coverUrl/videoUrl/contentMd 传 null 保持不变，"
                    + "categoryIds/tags 传 null 保持不变、传空数组清空；status 置 published 时校验标题与分类。")
    @SecurityRequirement(name = OpenApiConfiguration.BEARER_AUTH)
    @PutMapping("/{id}")
    public Result<WorkVO> update(@PathVariable String id, @RequestBody WorkUpdateDTO workUpdateDTO) {
        log.info("修改作品：{}", workUpdateDTO);
        WorkVO result = workService.update(id, workUpdateDTO);
        return Result.success(result);
    }

    @Operation(summary = "点赞作品")
    @SecurityRequirement(name = OpenApiConfiguration.BEARER_AUTH)
    @PostMapping("/{id}/like")
    public Result<WorkLikeResultVO> like(@PathVariable String id) {
        log.info("点赞作品：{}", id);
        return Result.success(workService.like(id));
    }

    @Operation(summary = "取消点赞")
    @SecurityRequirement(name = OpenApiConfiguration.BEARER_AUTH)
    @DeleteMapping("/{id}/like")
    public Result<WorkLikeResultVO> unlike(@PathVariable String id) {
        log.info("取消点赞：{}", id);
        return Result.success(workService.unlike(id));
    }

    @Operation(summary = "收藏作品")
    @SecurityRequirement(name = OpenApiConfiguration.BEARER_AUTH)
    @PostMapping("/{id}/favorite")
    public Result<FavoriteResultVO> favorite(@PathVariable String id) {
        log.info("收藏作品：{}", id);
        return Result.success(workService.favorite(id));
    }

    @Operation(summary = "取消收藏")
    @SecurityRequirement(name = OpenApiConfiguration.BEARER_AUTH)
    @DeleteMapping("/{id}/favorite")
    public Result<FavoriteResultVO> unfavorite(@PathVariable String id) {
        log.info("取消收藏：{}", id);
        return Result.success(workService.unfavorite(id));
    }
}
