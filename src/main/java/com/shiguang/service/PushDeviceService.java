package com.shiguang.service;

import com.shiguang.dto.PushDeviceRegisterDTO;

/**
 * 推送设备注册服务。
 *
 * 设备归属以当前登录用户为准；同一用户同一平台同一厂商仅保留一个 token。
 */
public interface PushDeviceService {

    /** 注册/覆盖当前用户的某平台某厂商设备 token。 */
    void register(PushDeviceRegisterDTO dto);

    /** 注销当前用户某平台某厂商设备。 */
    void unregister(String platform, String vendor);
}
