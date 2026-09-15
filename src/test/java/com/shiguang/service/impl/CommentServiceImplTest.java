package com.shiguang.service.impl;

import com.github.pagehelper.Page;
import com.shiguang.context.UserContext;
import com.shiguang.dto.CommentCreateDTO;
import com.shiguang.entity.Comment;
import com.shiguang.entity.Work;
import com.shiguang.exception.BusinessException;
import com.shiguang.mapper.CommentMapper;
import com.shiguang.mapper.WorkMapper;
import com.shiguang.service.NotificationService;
import com.shiguang.service.PushService;
import com.shiguang.service.RealtimePushService;
import com.shiguang.vo.CommentVO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentServiceImplTest {
    @Mock private CommentMapper commentMapper;
    @Mock private WorkMapper workMapper;
    @Mock private NotificationService notificationService;
    @Mock private PushService pushService;
    @Mock private RealtimePushService realtimePushService;
    @InjectMocks private CommentServiceImpl commentService;

    @BeforeEach
    void setUp() { UserContext.setCurrentId("u-replier"); }

    @AfterEach
    void tearDown() { UserContext.remove(); }

    @Test
    void create_shouldAllowReplyToAnyDepthAndNotifyParentAuthor() {
        Work work = new Work(); work.setId("w-1"); work.setStatus("published"); work.setAuthorId("u-author"); work.setTitle("作品");
        Comment parent = new Comment(); parent.setId(7L); parent.setWorkId("w-1"); parent.setAuthorId("u-parent");
        CommentVO created = CommentVO.builder().id(8L).build();
        when(workMapper.selectById("w-1")).thenReturn(work);
        when(commentMapper.selectById(7L)).thenReturn(parent);
        when(commentMapper.pageByWork(eq("w-1"), eq("u-replier"))).thenReturn(pageOf(created));
        doAnswer(invocation -> { ((Comment) invocation.getArgument(0)).setId(8L); return 1; })
                .when(commentMapper).insert(any(Comment.class));
        when(notificationService.create(eq("u-parent"), eq("u-replier"), eq("w-1"), eq("reply"), eq(8L), any(String.class))).thenReturn(9L);

        CommentCreateDTO request = new CommentCreateDTO(); request.setContent("第三层回复"); request.setParentId(7L);
        commentService.create("w-1", request);

        verify(commentMapper).insert(any(Comment.class));
        verify(notificationService).create(eq("u-parent"), eq("u-replier"), eq("w-1"), eq("reply"), eq(8L), any(String.class));
    }

    @Test
    void create_shouldRejectParentFromAnotherWork() {
        Work work = new Work(); work.setId("w-1"); work.setStatus("published"); work.setAuthorId("u-author");
        Comment parent = new Comment(); parent.setId(7L); parent.setWorkId("w-other"); parent.setAuthorId("u-parent");
        when(workMapper.selectById("w-1")).thenReturn(work);
        when(commentMapper.selectById(7L)).thenReturn(parent);
        CommentCreateDTO request = new CommentCreateDTO(); request.setContent("非法回复"); request.setParentId(7L);

        assertThrows(BusinessException.class, () -> commentService.create("w-1", request));
        verify(commentMapper, never()).insert(any(Comment.class));
    }

    private Page<CommentVO> pageOf(CommentVO item) {
        Page<CommentVO> page = new Page<>(1, 20);
        page.add(item);
        return page;
    }
}
