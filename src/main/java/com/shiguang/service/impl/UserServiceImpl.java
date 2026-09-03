package com.shiguang.service.impl;

import com.shiguang.context.UserContext;
import com.shiguang.dto.UpdateUserDTO;
import com.shiguang.entity.User;
import com.shiguang.exception.BusinessException;
import com.shiguang.mapper.UserMapper;
import com.shiguang.service.UserService;
import com.shiguang.vo.UserInfoVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;

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
                .build();
    }
}
