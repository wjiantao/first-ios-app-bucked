package com.shiguang.service.impl;

import com.shiguang.mapper.TagMapper;
import com.shiguang.service.TagService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 标签服务实现。
 */
@Service
@Slf4j
public class TagServiceImpl implements TagService {

    @Autowired
    private TagMapper tagMapper;

    @Override
    public List<String> list() {
        return tagMapper.listNames();
    }
}
