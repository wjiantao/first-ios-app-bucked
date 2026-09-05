package com.shiguang.controller;

import com.shiguang.config.OpenApiConfiguration;
import com.shiguang.context.UserContext;
import com.shiguang.dto.ProfilePageQueryDTO;
import com.shiguang.dto.UpdateUserDTO;
import com.shiguang.entity.User;
import com.shiguang.result.PageResult;
import com.shiguang.result.Result;
import com.shiguang.service.AuthService;
import com.shiguang.service.UserService;
import com.shiguang.service.WorkService;
import com.shiguang.vo.UserInfoVO;
import com.shiguang.vo.WorkVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

/**
 * 用户接口（需登录）。
 *
 * GET /api/users/me 返回当前登录用户信息，供 App 启动时恢复会话或展示“我的”。
 */
@RestController
@RequestMapping(value = "/api/users", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "用户接口", description = "需携带 JWT 访问")
@Slf4j
@SecurityRequirement(name = OpenApiConfiguration.BEARER_AUTH)
public class UserController {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserService userService;

    @Autowired
    private WorkService workService;

    @Operation(summary = "获取当前登录用户信息",
            description = "从 JWT 解析当前用户 ID 并返回完整资料，供 App 启动时恢复会话或“我的”页展示。")
    @GetMapping("/me")
    public Result<UserInfoVO> me() {
        return Result.success(authService.getCurrentUser(UserContext.getCurrentId()));
    }

    /**
     * 修改用户信息
     * @param updateUserDTO
     * @return
     */
    @Operation(summary = "修改当前用户资料",
            description = "局部更新：请求体中传 null 的字段保持不变，只更新传入的非空字段"
                    + "（nickname/bio/avatarUrl/tagline）。email、status 不允许客户端修改。")
    @PostMapping("/update")
    public Result<UserInfoVO> update(@RequestBody UpdateUserDTO updateUserDTO) {
        log.info("用户修改: {}", updateUserDTO);
        return Result.success(userService.update(updateUserDTO));
    }

    /**
     * 我的作品分页（需登录）。
     *
     * 返回当前用户已发布的作品，按发布时间倒序，
     * 供个人主页“作品”Tab 使用。
     */
    @Operation(summary = "我的作品（分页）",
            description = "从 JWT 解析当前用户，返回其已发布作品，按发布时间倒序。")
    @GetMapping("/me/works")
    public Result<PageResult<WorkVO>> myWorks(ProfilePageQueryDTO profilePageQueryDTO) {
        log.info("我的作品分页：{}", profilePageQueryDTO);
        return Result.success(workService.pageMyWorks(profilePageQueryDTO));
    }

    /**
     * 我的作品管理分页（需登录）。
     *
     * 与“我的作品”不同：作品管理页需要同时看到“已发布 + 已下架”的作品，
     * 以便作者对被隐藏的作品执行重新发布；个人主页“作品”Tab 仍只看已发布。
     */
    @Operation(summary = "我的作品管理（分页）",
            description = "从 JWT 解析当前用户，返回其已发布及已下架作品，按发布时间倒序；"
                    + "供“作品管理”页展示，便于下架与重新发布。")
    @GetMapping("/me/manage-works")
    public Result<PageResult<WorkVO>> myManageWorks(ProfilePageQueryDTO profilePageQueryDTO) {
        log.info("我的作品管理分页：{}", profilePageQueryDTO);
        return Result.success(workService.pageMyManageWorks(profilePageQueryDTO));
    }

    /**
     * 我的草稿分页（需登录）。
     *
     * 返回当前用户的草稿作品，按创建时间倒序，
     * 供个人主页"草稿"Tab 使用。草稿属于未发布的私密内容，仅作者本人可见。
     */
    @Operation(summary = "我的草稿（分页）",
            description = "从 JWT 解析当前用户，返回其草稿作品，按创建时间倒序。")
    @GetMapping("/me/drafts")
    public Result<PageResult<WorkVO>> myDrafts(ProfilePageQueryDTO profilePageQueryDTO) {
        log.info("我的草稿分页：{}", profilePageQueryDTO);
        return Result.success(workService.pageMyDrafts(profilePageQueryDTO));
    }

    /**
     * 我的收藏分页（需登录）。
     *
     * 返回当前用户收藏的已发布作品，按收藏时间倒序，
     * 供个人主页"收藏"Tab 使用。
     */
    @Operation(summary = "我的收藏（分页）",
            description = "从 JWT 解析当前用户，返回收藏的已发布作品，按收藏时间倒序。")
    @GetMapping("/me/favorites")
    public Result<PageResult<WorkVO>> myFavorites(ProfilePageQueryDTO profilePageQueryDTO) {
        log.info("我的收藏分页：{}", profilePageQueryDTO);
        return Result.success(workService.pageMyFavorites(profilePageQueryDTO));
    }

    /**
     * 我的喜欢分页（需登录）。
     *
     * 返回当前用户点赞过的已发布作品，按点赞时间倒序，
     * 供个人主页“喜欢”Tab 使用。
     */
    @Operation(summary = "我的喜欢（分页）",
            description = "从 JWT 解析当前用户，返回其点赞过的已发布作品，按点赞时间倒序。")
    @GetMapping("/me/likes")
    public Result<PageResult<WorkVO>> myLikes(ProfilePageQueryDTO profilePageQueryDTO) {
        log.info("我的喜欢分页：{}", profilePageQueryDTO);
        return Result.success(workService.pageMyLikes(profilePageQueryDTO));
    }
}
