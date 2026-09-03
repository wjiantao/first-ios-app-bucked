package com.shiguang.service.impl;

import com.shiguang.mapper.CategoryMapper;
import com.shiguang.service.CategoryService;
import com.shiguang.vo.CategoryVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 分类服务实现。
 */
@Service
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public List<CategoryVO> list() {
        return categoryMapper.list();
    }
}
