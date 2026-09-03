package com.shiguang.service;

import com.shiguang.vo.CategoryVO;

import java.util.List;

/**
 * 分类服务接口。
 */
public interface CategoryService {

    /**
     * 查询启用中的全部分类，按 sortOrder 升序。
     *
     * @return 分类列表
     */
    List<CategoryVO> list();
}
