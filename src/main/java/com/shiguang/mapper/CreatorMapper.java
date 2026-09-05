package com.shiguang.mapper;

import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

/**
 * 创作者数据概览数据访问接口。
 *
 * 与 WorkMapper 一致：SQL 收敛在 resources/mapper/CreatorMapper.xml 中。
 * 统计口径统一为「当前用户已发布/下架的作品」，草稿与已删除不计入。
 */
public interface CreatorMapper {

    /**
     * 统计当前用户所有已发布/下架作品收到的浏览总数。
     *
     * @param authorId 当前登录用户 ID
     * @return 累计浏览量
     */
    long countViews(@Param("authorId") String authorId);

    /**
     * 统计当前用户所有已发布/下架作品中，今日（服务端本地时区自然日）新增的浏览量。
     *
     * <p>起始时间由调用方以 Java 的 {@code LocalDate.now().atStartOfDay()} 计算后传入，
     * 而不是用数据库的 CURDATE()，避免应用与数据库时区不一致导致“今日”口径漂移。</p>
     *
     * @param authorId     当前登录用户 ID
     * @param startOfToday 今日 0 点（含）
     * @return 今日新增浏览量
     */
    long countTodayViews(@Param("authorId") String authorId,
                         @Param("startOfToday") LocalDateTime startOfToday);

    /**
     * 统计当前用户所有已发布/下架作品收获的点赞总数。
     *
     * @param authorId 当前登录用户 ID
     * @return 累计点赞数
     */
    long countLikes(@Param("authorId") String authorId);
}
