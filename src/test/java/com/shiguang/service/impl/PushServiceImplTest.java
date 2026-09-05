package com.shiguang.service.impl;

import com.shiguang.entity.PushDevice;
import com.shiguang.entity.User;
import com.shiguang.mapper.NotificationMapper;
import com.shiguang.mapper.PushDeviceMapper;
import com.shiguang.mapper.UserMapper;
import com.shiguang.properties.PushProperties;
import com.shiguang.push.JPushRestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PushServiceImplTest {

    @Mock
    private PushProperties pushProperties;
    @Mock
    private PushDeviceMapper pushDeviceMapper;
    @Mock
    private UserMapper userMapper;
    @Mock
    private NotificationMapper notificationMapper;
    @Mock
    private JPushRestClient jPushRestClient;

    @InjectMocks
    private PushServiceImpl pushService;

    private PushDevice jpushDevice() {
        PushDevice device = new PushDevice();
        device.setPlatform("android");
        device.setVendor("jpush");
        device.setToken("reg-id-1");
        return device;
    }

    @BeforeEach
    void setUp() {
        when(pushProperties.isConfigured()).thenReturn(true);
        User actor = new User();
        actor.setNickname("小北");
        when(userMapper.getById("actor-1")).thenReturn(actor);
        when(notificationMapper.countUnread("author-1")).thenReturn(3L);
    }

    @Test
    void onInteraction_shouldSendJpushWithExpectedExtras() throws Exception {
        when(pushDeviceMapper.selectByUserId("author-1"))
                .thenReturn(List.of(jpushDevice()));

        pushService.onInteraction(
                "author-1", "actor-1", "w-1", "我的作品", 12L, "like");

        verify(jPushRestClient).sendToRegistrationId(
                eq("reg-id-1"),
                eq("新点赞"),
                eq("小北 赞了你的作品《我的作品》"),
                argThat((Map<String, String> extras) ->
                        "like".equals(extras.get("type"))
                                && "w-1".equals(extras.get("workId"))
                                && "12".equals(extras.get("notificationId"))
                                && "小北".equals(extras.get("actorNickname"))
                                && "我的作品".equals(extras.get("workTitle"))),
                eq(3L));
    }

    @Test
    void onInteraction_shouldSkipUnsupportedVendor() throws Exception {
        PushDevice legacy = new PushDevice();
        legacy.setPlatform("android");
        legacy.setVendor("fcm");
        legacy.setToken("old-token");
        when(pushDeviceMapper.selectByUserId("author-1")).thenReturn(List.of(legacy));

        pushService.onInteraction(
                "author-1", "actor-1", "w-1", "我的作品", 12L, "like");

        verify(jPushRestClient, never()).sendToRegistrationId(
                anyString(), anyString(), anyString(), anyMap(), anyLong());
    }

    @Test
    void onInteraction_shouldContinueAfterSingleDeviceFailure() throws Exception {
        PushDevice second = new PushDevice();
        second.setPlatform("ios");
        second.setVendor("jpush");
        second.setToken("reg-id-2");
        when(pushDeviceMapper.selectByUserId("author-1"))
                .thenReturn(List.of(jpushDevice(), second));

        doThrow(new IllegalStateException("network error"))
                .doNothing()
                .when(jPushRestClient)
                .sendToRegistrationId(
                        anyString(), anyString(), anyString(), anyMap(), anyLong());

        pushService.onInteraction(
                "author-1", "actor-1", "w-1", "我的作品", 12L, "like");

        verify(jPushRestClient, org.mockito.Mockito.times(2))
                .sendToRegistrationId(
                        anyString(), anyString(), anyString(), anyMap(), anyLong());
    }
}
