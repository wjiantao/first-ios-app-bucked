package com.shiguang.controller;

import com.shiguang.config.OpenApiConfiguration;
import com.shiguang.dto.ProfilePageQueryDTO;
import com.shiguang.result.PageResult;
import com.shiguang.result.Result;
import com.shiguang.service.NotificationService;
import com.shiguang.vo.NotificationVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

/**
 * 站内通知接口（需登录）。
 *
 * 消息中心：当他人点赞/收藏了当前用户的作品后，作者在此查看通知列表、未读数，并执行已读。
 */
@RestController
@RequestMapping(value = "/api/notifications", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "通知接口", description = "需携带 JWT 访问")
@Slf4j
@SecurityRequirement(name = OpenApiConfiguration.BEARER_AUTH)
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Operation(summary = "通知列表（分页）",
            description = "按 current/pageSize 分页，返回当前用户的点赞/收藏通知，按时间倒序。")
    @GetMapping
    public Result<PageResult<NotificationVO>> page(ProfilePageQueryDTO profilePageQueryDTO) {
        log.info("通知列表分页：{}", profilePageQueryDTO);
        return Result.success(notificationService.page(profilePageQueryDTO));
    }

    @Operation(summary = "未读通知数",
            description = "返回当前用户未读通知数量，供 App 消息角标展示。")
    @GetMapping("/unread-count")
    public Result<Long> unreadCount() {
        return Result.success(notificationService.unreadCount());
    }

    @Operation(summary = "全部标记已读",
            description = "把当前用户所有未读通知置为已读，返回本次处理条数。")
    @PostMapping("/read-all")
    public Result<Integer> readAll() {
        int count = notificationService.markAllRead();
        log.info("全部标记已读：{}", count);
        return Result.success(count);
    }

    @Operation(summary = "单条标记已读",
            description = "把当前用户指定通知置为已读，返回影响行数（0 表示不存在或不属于当前用户）。")
    @PostMapping("/{id}/read")
    public Result<Integer> markRead(@PathVariable Long id) {
        int count = notificationService.markRead(id);
        log.info("单条标记已读 id={} count={}", id, count);
        return Result.success(count);
    }
}
