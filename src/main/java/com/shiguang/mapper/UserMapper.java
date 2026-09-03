package com.shiguang.mapper;

import com.shiguang.entity.User;

/**
 * 用户表数据访问接口（邮箱注册/账号登录相关）。
 *
 * SQL 全部收敛在 resources/mapper/UserMapper.xml 中，
 * 接口只声明方法签名，与 sky-take-out 的 Mapper 写法保持一致。
 */
public interface UserMapper {

    User getById(String id);

    User getByEmail(String email);

    int insert(User user);

    /** 注册第二步：pending 用户设置密码后升级为 active。 */
    int completeRegistration(User user);

    int update(User user);

}
