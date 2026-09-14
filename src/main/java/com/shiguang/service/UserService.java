package com.shiguang.service;

import com.shiguang.dto.UpdateUserDTO;
import com.shiguang.vo.UserInfoVO;
import com.shiguang.vo.FollowResultVO;

public interface UserService {
    /** 修改当前用户头像（avatarUrl 为上传后返回的 /uploads/xxx 路径）。 */
    void updateAvatar(String userId, String avatarUrl);

    /**
     * 修改用户信息
     *
     * @param updateUserDTO
     * @return
     */
    UserInfoVO update(UpdateUserDTO updateUserDTO);

    /** 关注目标用户。 */
    FollowResultVO follow(String targetUserId);

    /** 取消关注目标用户。 */
    FollowResultVO unfollow(String targetUserId);



}
