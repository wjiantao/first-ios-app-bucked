package com.shiguang.service;

import com.shiguang.vo.CreatorStatsVO;

/**
 * 创作者数据概览服务接口。
 */
public interface CreatorService {

    /**
     * 获取当前登录用户的数据概览（累计浏览量/今日新增浏览量/累计点赞）。
     *
     * @return 数据概览，返回原始整型计数，展示格式化由前端完成
     */
    CreatorStatsVO stats();
}
