package com.shiguang.mapper;

import com.github.pagehelper.Page;
import com.shiguang.entity.Comment;
import com.shiguang.vo.CommentVO;
import org.apache.ibatis.annotations.Param;

/** 评论数据访问接口。 */
public interface CommentMapper {
    Page<CommentVO> pageByWork(@Param("workId") String workId, @Param("userId") String userId);
    Comment selectById(@Param("id") Long id);
    int insert(Comment comment);
    int softDelete(@Param("id") Long id, @Param("authorId") String authorId);
}
