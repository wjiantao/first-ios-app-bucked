package com.shiguang.service;

import com.shiguang.dto.CommentCreateDTO;
import com.shiguang.result.PageResult;
import com.shiguang.vo.CommentVO;

/** 作品评论业务。 */
public interface CommentService {
    PageResult<CommentVO> page(String workId, int current, int pageSize);
    CommentVO create(String workId, CommentCreateDTO request);
    void delete(Long commentId);
}
