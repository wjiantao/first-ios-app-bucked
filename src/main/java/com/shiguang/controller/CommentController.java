package com.shiguang.controller;

import com.shiguang.config.OpenApiConfiguration;
import com.shiguang.dto.CommentCreateDTO;
import com.shiguang.result.PageResult;
import com.shiguang.result.Result;
import com.shiguang.service.CommentService;
import com.shiguang.vo.CommentVO;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/** 作品评论接口；评论详情包含用户信息，因此统一要求登录。 */
@RestController
@RequestMapping("/api")
@SecurityRequirement(name = OpenApiConfiguration.BEARER_AUTH)
public class CommentController {
    @Autowired private CommentService commentService;

    @GetMapping("/works/{workId}/comments")
    public Result<PageResult<CommentVO>> page(@PathVariable String workId,
                                               @RequestParam(defaultValue = "1") int current,
                                               @RequestParam(defaultValue = "20") int pageSize) {
        return Result.success(commentService.page(workId, current, pageSize));
    }

    @PostMapping("/works/{workId}/comments")
    public Result<CommentVO> create(@PathVariable String workId, @RequestBody CommentCreateDTO request) {
        return Result.success(commentService.create(workId, request));
    }

    @DeleteMapping("/comments/{commentId}")
    public Result<Void> delete(@PathVariable Long commentId) {
        commentService.delete(commentId);
        return Result.success();
    }
}
