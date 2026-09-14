package com.shiguang.mapper;

import org.apache.ibatis.annotations.Param;

/**
 * 用户关注关系数据访问接口。
 *
 * 关注关系单独存储，不把粉丝数冗余到 users 表，保证关注/取消关注后统计值
 * 始终由关系表聚合得到，避免维护两份可变数据。
 */
public interface FollowMapper {

    /** 新增关注关系；重复关系由联合主键保证幂等。 */
    int insert(@Param("followerId") String followerId,
               @Param("followingId") String followingId);

    /** 删除关注关系；不存在时影响行数为 0，天然幂等。 */
    int delete(@Param("followerId") String followerId,
               @Param("followingId") String followingId);

    /** 查询当前用户是否关注目标用户。 */
    boolean exists(@Param("followerId") String followerId,
                   @Param("followingId") String followingId);

    /** 统计目标用户的粉丝数。 */
    long countFollowers(@Param("followingId") String followingId);
}
