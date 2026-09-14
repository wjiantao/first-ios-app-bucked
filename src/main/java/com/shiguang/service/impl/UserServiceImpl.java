package com.shiguang.service.impl;

import com.shiguang.context.UserContext;
import com.shiguang.dto.UpdateUserDTO;
import com.shiguang.entity.User;
import com.shiguang.exception.BusinessException;
import com.shiguang.mapper.UserMapper;
import com.shiguang.mapper.FollowMapper;
import com.shiguang.service.NotificationService;
import com.shiguang.service.UserService;
import com.shiguang.service.RealtimePushService;
import com.shiguang.vo.FollowResultVO;
import com.shiguang.vo.UserInfoVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private FollowMapper followMapper;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private RealtimePushService realtimePushService;

    @Override
    public void updateAvatar(String userId, String avatarUrl) {
        User user = new User();
        user.setId(userId);
        user.setAvatarUrl(avatarUrl);
        userMapper.update(user);
    }

    @Override
    public UserInfoVO update(UpdateUserDTO updateUserDTO) {
        User user = new User();
        BeanUtils.copyProperties(updateUserDTO, user);
        user.setId(UserContext.getCurrentId());
        userMapper.update(user);

        User updated = userMapper.getById(user.getId());
        if (updated == null) {
            throw new BusinessException(404, "用户不存在");
        }
        return UserInfoVO.builder()
                .id(updated.getId())
                .nickname(updated.getNickname())
                .avatarUrl(updated.getAvatarUrl())
                .bio(updated.getBio())
                .tagline(updated.getTagline())
                .followerCount(followMapper.countFollowers(updated.getId()))
                .build();
    }

    /**
     * 关注目标用户。
     *
     * 只有首次插入关系时才生成通知；重复点击关注不会重复打扰被关注者。
     */
    @Override
    public FollowResultVO follow(String targetUserId) {
        String followerId = UserContext.getCurrentId();
        validateTarget(followerId, targetUserId);
        requireUser(targetUserId);
        int inserted = followMapper.insert(followerId, targetUserId);
        long followerCount = followMapper.countFollowers(targetUserId);
        if (inserted > 0) {
            long notificationId = notificationService.create(
                    targetUserId, followerId, null, "follow");
            realtimePushService.push(
                    targetUserId, followerId, null, null, notificationId, "follow");
        }
        return FollowResultVO.builder()
                .following(true)
                .followerCount(followerCount)
                .build();
    }

    /**
     * 取消关注目标用户。
     *
     * 取消操作不删除历史关注通知，避免用户已经收到的消息因关系变化而消失。
     */
    @Override
    public FollowResultVO unfollow(String targetUserId) {
        String followerId = UserContext.getCurrentId();
        validateTarget(followerId, targetUserId);
        requireUser(targetUserId);
        followMapper.delete(followerId, targetUserId);
        return FollowResultVO.builder()
                .following(false)
                .followerCount(followMapper.countFollowers(targetUserId))
                .build();
    }

    private void validateTarget(String followerId, String targetUserId) {
        if (targetUserId == null || targetUserId.isBlank()) {
            throw new BusinessException(400, "目标用户不存在");
        }
        if (followerId.equals(targetUserId)) {
            throw new BusinessException(400, "不能关注自己");
        }
    }

    private User requireUser(String userId) {
        User user = userMapper.getById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        return user;
    }
}
