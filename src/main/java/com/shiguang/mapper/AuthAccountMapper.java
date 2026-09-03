package com.shiguang.mapper;

import com.shiguang.entity.AuthAccount;
import com.shiguang.entity.User;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 第三方登录绑定关系数据访问接口。
 *
 * SQL 全部收敛在 resources/mapper/AuthAccountMapper.xml 中。
 */
public interface AuthAccountMapper {

    AuthAccount getByChannelAndUid(@Param("channel") String channel,
                                   @Param("channelUid") String channelUid);

    int insert(AuthAccount authAccount);

}
