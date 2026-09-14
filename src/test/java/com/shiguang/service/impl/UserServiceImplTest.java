package com.shiguang.service.impl;

import com.shiguang.context.UserContext;
import com.shiguang.entity.User;
import com.shiguang.exception.BusinessException;
import com.shiguang.mapper.FollowMapper;
import com.shiguang.mapper.UserMapper;
import com.shiguang.service.NotificationService;
import com.shiguang.service.RealtimePushService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private FollowMapper followMapper;

    @Mock
    private NotificationService notificationService;

    @Mock
    private RealtimePushService realtimePushService;

    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        UserContext.setCurrentId("u-follower");
    }

    @AfterEach
    void tearDown() {
        UserContext.remove();
    }

    @Test
    void follow_shouldCreateRelationNotificationAndReturnCount() {
        when(userMapper.getById("u-target")).thenReturn(new User());
        when(followMapper.insert("u-follower", "u-target")).thenReturn(1);
        when(followMapper.countFollowers("u-target")).thenReturn(3L);
        when(notificationService.create("u-target", "u-follower", null, "follow"))
                .thenReturn(8L);

        var result = userService.follow("u-target");

        assertEquals(true, result.isFollowing());
        assertEquals(3L, result.getFollowerCount());
        verify(notificationService).create("u-target", "u-follower", null, "follow");
        verify(realtimePushService).push(
                "u-target", "u-follower", null, null, 8L, "follow");
    }

    @Test
    void repeatedFollow_shouldNotCreateSecondNotification() {
        when(userMapper.getById("u-target")).thenReturn(new User());
        when(followMapper.insert("u-follower", "u-target")).thenReturn(0);
        when(followMapper.countFollowers("u-target")).thenReturn(3L);

        userService.follow("u-target");

        verify(notificationService, never()).create(
                eq("u-target"), eq("u-follower"), eq(null), eq("follow"));
        verify(realtimePushService, never()).push(
                eq("u-target"), eq("u-follower"), eq(null), eq(null), eq(0L), eq("follow"));
    }

    @Test
    void follow_shouldRejectFollowingSelf() {
        assertThrows(BusinessException.class, () -> userService.follow("u-follower"));
        verify(userMapper, never()).getById("u-follower");
        verify(followMapper, never()).insert("u-follower", "u-follower");
    }

    @Test
    void unfollow_shouldBeIdempotentAndReturnLatestCount() {
        when(userMapper.getById("u-target")).thenReturn(new User());
        when(followMapper.countFollowers("u-target")).thenReturn(2L);

        var result = userService.unfollow("u-target");

        assertEquals(false, result.isFollowing());
        assertEquals(2L, result.getFollowerCount());
        verify(followMapper).delete("u-follower", "u-target");
        verify(notificationService, never()).create(
                eq("u-target"), eq("u-follower"), eq(null), eq("follow"));
    }
}
