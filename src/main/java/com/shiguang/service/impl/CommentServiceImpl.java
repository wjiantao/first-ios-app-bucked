package com.shiguang.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.shiguang.context.UserContext;
import com.shiguang.dto.CommentCreateDTO;
import com.shiguang.entity.Comment;
import com.shiguang.entity.Work;
import com.shiguang.exception.BusinessException;
import com.shiguang.mapper.CommentMapper;
import com.shiguang.mapper.WorkMapper;
import com.shiguang.result.PageResult;
import com.shiguang.service.CommentService;
import com.shiguang.service.NotificationService;
import com.shiguang.service.PushService;
import com.shiguang.service.RealtimePushService;
import com.shiguang.vo.CommentVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/** 评论业务实现；数据校验集中在此层，Controller 不承载业务规则。 */
@Service
public class CommentServiceImpl implements CommentService {
    @Autowired private CommentMapper commentMapper;
    @Autowired private WorkMapper workMapper;
    @Autowired private NotificationService notificationService;
    @Autowired private PushService pushService;
    @Autowired private RealtimePushService realtimePushService;

    @Override
    public PageResult<CommentVO> page(String workId, int current, int pageSize) {
        requirePublishedWork(workId);
        PageHelper.startPage(Math.max(current, 1), Math.min(Math.max(pageSize, 1), 50));
        Page<CommentVO> page = commentMapper.pageByWork(workId, UserContext.getCurrentId());
        return new PageResult<>(page.getTotal(), page.getResult());
    }

    @Override
    @Transactional
    public CommentVO create(String workId, CommentCreateDTO request) {
        Work work = requirePublishedWork(workId);
        String content = request.getContent() == null ? "" : request.getContent().trim();
        String image = normalize(request.getImageUrl());
        if (content.isEmpty() && image == null) throw new BusinessException("评论内容或图片不能为空");
        if (content.length() > 1000) throw new BusinessException("评论内容不能超过 1000 个字符");
        if (image != null && !image.matches("^(/uploads/|https?://).+")) throw new BusinessException("评论图片地址无效");
        Comment parent = null;
        if (request.getParentId() != null) {
            parent = commentMapper.selectById(request.getParentId());
            if (parent == null || !workId.equals(parent.getWorkId())) throw new BusinessException("回复目标不存在");
        }
        Comment comment = new Comment();
        comment.setWorkId(workId); comment.setAuthorId(UserContext.getCurrentId()); comment.setParentId(request.getParentId());
        comment.setContent(content); comment.setImageUrl(image); comment.setCreatedAt(LocalDateTime.now());
        commentMapper.insert(comment);
        String recipient = parent == null ? work.getAuthorId() : parent.getAuthorId();
        String type = parent == null ? "comment" : "reply";
        if (!recipient.equals(comment.getAuthorId())) {
            String excerpt = content.isEmpty() ? "发送了一张图片" : content.substring(0, Math.min(content.length(), 255));
            long notificationId = notificationService.create(recipient, comment.getAuthorId(), workId, type, comment.getId(), excerpt);
            pushService.onInteraction(recipient, comment.getAuthorId(), workId, work.getTitle(), notificationId, type);
            realtimePushService.push(recipient, comment.getAuthorId(), workId, work.getTitle(), notificationId, type);
        }
        return commentMapper.pageByWork(workId, UserContext.getCurrentId()).stream()
                .filter(item -> item.getId().equals(comment.getId())).findFirst().orElseThrow();
    }

    @Override
    @Transactional
    public void delete(Long commentId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null || !comment.getAuthorId().equals(UserContext.getCurrentId())) throw new BusinessException(404, "评论不存在或无权删除");
        commentMapper.softDelete(commentId, UserContext.getCurrentId());
    }

    private Work requirePublishedWork(String workId) {
        Work work = workMapper.selectById(workId);
        if (work == null || !"published".equals(work.getStatus())) throw new BusinessException(404, "作品不存在或已下架");
        return work;
    }

    private String normalize(String value) { if (value == null) return null; String v = value.trim(); return v.isEmpty() ? null : v; }
}
