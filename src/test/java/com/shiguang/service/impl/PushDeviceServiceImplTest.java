package com.shiguang.service.impl;

import com.shiguang.context.UserContext;
import com.shiguang.dto.PushDeviceRegisterDTO;
import com.shiguang.exception.BusinessException;
import com.shiguang.mapper.PushDeviceMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PushDeviceServiceImplTest {

    @Mock
    private PushDeviceMapper pushDeviceMapper;

    @InjectMocks
    private PushDeviceServiceImpl pushDeviceService;

    @BeforeEach
    void setUp() {
        UserContext.setCurrentId("u-1");
    }

    @AfterEach
    void tearDown() {
        UserContext.remove();
    }

    @Test
    void register_shouldUpsertWithVendor() {
        PushDeviceRegisterDTO dto = new PushDeviceRegisterDTO();
        dto.setPlatform("android");
        dto.setVendor("jpush");
        dto.setToken("reg-id-1");

        pushDeviceService.register(dto);

        verify(pushDeviceMapper).upsert(
                eq("u-1"),
                eq("android"),
                eq("jpush"),
                eq("reg-id-1"),
                any(LocalDateTime.class));
    }

    @Test
    void register_shouldRejectUnsupportedVendor() {
        PushDeviceRegisterDTO dto = new PushDeviceRegisterDTO();
        dto.setPlatform("android");
        dto.setVendor("fcm");
        dto.setToken("reg-id-1");

        assertThrows(BusinessException.class, () -> pushDeviceService.register(dto));
    }

    @Test
    void unregister_shouldDeleteByPlatformAndVendor() {
        pushDeviceService.unregister("ios", "jpush");

        verify(pushDeviceMapper).deleteByUserAndPlatform("u-1", "ios", "jpush");
    }
}
