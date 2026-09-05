package com.shiguang.service.impl;

import com.shiguang.context.UserContext;
import com.shiguang.dto.PushDeviceRegisterDTO;
import com.shiguang.exception.BusinessException;
import com.shiguang.mapper.PushDeviceMapper;
import com.shiguang.service.PushDeviceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * 推送设备注册服务实现。
 */
@Service
@Slf4j
public class PushDeviceServiceImpl implements PushDeviceService {

    private static final Set<String> PLATFORMS = Set.of("ios", "android");
    private static final Set<String> VENDORS = Set.of("jpush");

    @Autowired
    private PushDeviceMapper pushDeviceMapper;

    @Override
    public void register(PushDeviceRegisterDTO dto) {
        String platform = normalizePlatform(dto.getPlatform());
        String vendor = normalizeVendor(dto.getVendor());
        String token = dto.getToken();
        if (token == null || token.isBlank()) {
            throw new BusinessException("推送 token 不能为空");
        }
        String userId = UserContext.getCurrentId();
        pushDeviceMapper.upsert(userId, platform, vendor, token.trim(), LocalDateTime.now());
        log.info("推送设备注册成功 userId={} platform={} vendor={}", userId, platform, vendor);
    }

    @Override
    public void unregister(String platform, String vendor) {
        String normalized = normalizePlatform(platform);
        String normalizedVendor = normalizeVendor(vendor);
        String userId = UserContext.getCurrentId();
        pushDeviceMapper.deleteByUserAndPlatform(userId, normalized, normalizedVendor);
        log.info("推送设备注销成功 userId={} platform={} vendor={}",
                userId, normalized, normalizedVendor);
    }

    private String normalizePlatform(String platform) {
        if (platform == null || platform.isBlank()) {
            throw new BusinessException("设备平台不能为空");
        }
        String value = platform.trim().toLowerCase();
        if (!PLATFORMS.contains(value)) {
            throw new BusinessException("设备平台仅支持 ios / android");
        }
        return value;
    }

    private String normalizeVendor(String vendor) {
        if (vendor == null || vendor.isBlank()) {
            throw new BusinessException("推送厂商不能为空");
        }
        String value = vendor.trim().toLowerCase();
        if (!VENDORS.contains(value)) {
            throw new BusinessException("推送厂商仅支持 jpush");
        }
        return value;
    }
}
