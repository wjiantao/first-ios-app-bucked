package com.shiguang.mapper;

import com.shiguang.entity.PushDevice;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 推送设备表数据访问接口。
 *
 * 与 UserMapper 一致：SQL 收敛在 resources/mapper/PushDeviceMapper.xml 中。
 * 以 (user_id, platform, vendor) 唯一键 upsert，实现“同平台同厂商覆盖旧 token”。
 */
public interface PushDeviceMapper {

    /** 按 (user_id, platform, vendor) 插入或覆盖 token，返回影响行数。 */
    int upsert(@Param("userId") String userId,
               @Param("platform") String platform,
               @Param("vendor") String vendor,
               @Param("token") String token,
               @Param("now") LocalDateTime now);

    /** 注销当前用户某平台某厂商设备。 */
    int deleteByUserAndPlatform(@Param("userId") String userId,
                                @Param("platform") String platform,
                                @Param("vendor") String vendor);

    /** 查询某用户已注册的全部设备。 */
    List<PushDevice> selectByUserId(@Param("userId") String userId);
}
