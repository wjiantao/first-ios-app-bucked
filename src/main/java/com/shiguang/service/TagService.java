package com.shiguang.service;

import java.util.List;

/**
 * 标签服务接口。
 */
public interface TagService {

    /**
     * 查询库中已存在的全部标签名（去重，按名称升序）。
     *
     * 供发布页话题联想使用：候选即数据库已收录的标签，
     * 不包含热度等额外字段，用户可点选也可自行新建。
     *
     * @return 已存在的标签名列表
     */
    List<String> list();
}
