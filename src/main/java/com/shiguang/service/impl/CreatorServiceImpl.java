package com.shiguang.service.impl;

import com.shiguang.context.UserContext;
import com.shiguang.mapper.CreatorMapper;
import com.shiguang.service.CreatorService;
import com.shiguang.vo.CreatorStatsVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * 创作者数据概览服务实现。
 *
 * 从 JWT 拦截器写入的 UserContext 取当前用户 ID，聚合浏览与点赞两个维度。
 * 浏览量/点赞均按「该用户已发布/下架的作品」口径统计，草稿与已删除不计入。
 */
@Service
@Slf4j
public class CreatorServiceImpl implements CreatorService {

    @Autowired
    private CreatorMapper creatorMapper;

    @Override
    public CreatorStatsVO stats() {
        String authorId = UserContext.getCurrentId();
        // 今日以服务端本地时区 0 点为界，避免 DB 时区与应用不一致导致口径漂移。
        LocalDate today = LocalDate.now();

        long views = creatorMapper.countViews(authorId);
        long todayViews = creatorMapper.countTodayViews(authorId, today.atStartOfDay());
        long likes = creatorMapper.countLikes(authorId);
        log.info("创作者数据概览：userId={}, views={}, todayViews={}, likes={}", authorId, views, todayViews, likes);

        return CreatorStatsVO.builder()
                .views(views)
                .todayViews(todayViews)
                .likes(likes)
                .build();
    }
}
