package com.shiguang.service.impl;

import com.shiguang.entity.PushDevice;
import com.shiguang.entity.User;
import com.shiguang.mapper.NotificationMapper;
import com.shiguang.mapper.PushDeviceMapper;
import com.shiguang.mapper.UserMapper;
import com.shiguang.properties.PushProperties;
import com.shiguang.push.JPushRestClient;
import com.shiguang.service.PushService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统推送服务实现。
 *
 * iOS 与 Android 统一走极光 JPush。未配置 JPush 凭据时降级为仅记日志，
 * 不实际发送、不影响站内通知和点赞/收藏业务。
 */
@Service
@Slf4j
public class PushServiceImpl implements PushService {

    private static final String VENDOR_JPUSH = "jpush";

    @Autowired
    private PushProperties pushProperties;
    @Autowired
    private PushDeviceMapper pushDeviceMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private NotificationMapper notificationMapper;
    @Autowired
    private JPushRestClient jPushRestClient;

    @Async("pushExecutor")
    @Override
    public void onInteraction(String recipientId, String actorId, String workId,
                              String workTitle, long notificationId, String type) {
        if (!pushProperties.isConfigured()) {
            log.info("推送未配置，跳过推送（站内通知已写入）。recipientId={} actorId={} workId={} type={}",
                    recipientId, actorId, workId, type);
            return;
        }

        try {
            List<PushDevice> devices = pushDeviceMapper.selectByUserId(recipientId);
            if (devices.isEmpty()) {
                return;
            }

            User actor = userMapper.getById(actorId);
            String nickname = (actor != null && actor.getNickname() != null && !actor.getNickname().isBlank())
                    ? actor.getNickname() : "用户";
            boolean like = "like".equals(type);
            boolean favorite = "favorite".equals(type);
            String title = like ? "新点赞" : favorite ? "新收藏" : "新评论";
            String action = like ? "赞了你的作品《" : favorite ? "收藏了你的作品《" : ("reply".equals(type) ? "回复了你在《" : "评论了你的作品《");
            String bodyText = nickname + " " + action
                    + (workTitle != null ? workTitle : "你的作品") + "》";
            long badge = notificationMapper.countUnread(recipientId);

            Map<String, String> extras = new HashMap<>();
            extras.put("type", type);
            extras.put("workId", workId);
            extras.put("notificationId", String.valueOf(notificationId));
            extras.put("actorNickname", nickname);
            extras.put("workTitle", workTitle != null ? workTitle : "");

            for (PushDevice device : devices) {
                try {
                    if (!VENDOR_JPUSH.equals(device.getVendor())) {
                        log.info("跳过不支持的推送厂商 recipientId={} vendor={}",
                                recipientId, device.getVendor());
                        continue;
                    }
                    jPushRestClient.sendToRegistrationId(
                            device.getToken(), title, bodyText, extras, badge);
                    log.info("推送发送成功 recipientId={} platform={} vendor={} type={}",
                            recipientId, device.getPlatform(), device.getVendor(), type);
                } catch (Exception ex) {
                    log.error("推送发送失败 recipientId={} platform={} vendor={} type={}",
                            recipientId, device.getPlatform(), device.getVendor(), type, ex);
                }
            }
        } catch (Exception ex) {
            log.error("推送下发异常 recipientId={} type={}", recipientId, type, ex);
        }
    }
}
