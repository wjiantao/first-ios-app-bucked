package com.shiguang.service.impl;

import com.shiguang.constant.JwtClaimsConstant;
import com.shiguang.entity.AuthAccount;
import com.shiguang.entity.User;
import com.shiguang.exception.BusinessException;
import com.shiguang.mapper.AuthAccountMapper;
import com.shiguang.mapper.UserMapper;
import com.shiguang.mail.EmailSender;
import com.shiguang.properties.JwtProperties;
import com.shiguang.service.AuthService;
import com.shiguang.store.EmailCodeStore;
import com.shiguang.utils.JwtUtil;
import com.shiguang.utils.PasswordUtil;
import com.shiguang.vo.LoginVO;
import com.shiguang.vo.SendCodeVO;
import com.shiguang.vo.UserInfoVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;

/**
 * 认证服务实现。
 *
 * 注册采用“邮箱验证 -> 设置密码”两步：
 * 第一步只证明邮箱归属并创建 pending 用户，
 * 第二步才写入密码并把账号置为 active；
 * 这样密码设置失败时用户信息不会残缺，也不存在“未验证邮箱却已注册成功”的情况。
 *
 * 验证码发送：配置了 SMTP（spring.mail.host）时真实发信；
 * 未配置时仅开发模式（shiguang.dev-mode=true）允许走日志/回显通道，否则直接报错。
 *
 * 开发模式约定（shiguang.dev-mode=true 时生效）：
 * 1. 发送邮箱验证码接口会在响应里返回 devCode；
 * 2. 固定验证码 123456 可直接通过验证，方便没有真实邮件通道时联调。
 * 生产环境将 dev-mode 置为 false 后，两个便利行为同时失效。
 */
@Service
@Slf4j
public class AuthServiceImpl implements AuthService {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final String DEV_CODE = "123456";
    private static final int PASSWORD_MIN_LENGTH = 6;
    private static final String STATUS_PENDING = "pending";
    private static final String STATUS_ACTIVE = "active";
    private static final List<String> SOCIAL_CHANNELS = List.of("wechat", "douyin", "apple");

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private AuthAccountMapper authAccountMapper;
    @Autowired
    private JwtProperties jwtProperties;
    @Autowired
    private EmailCodeStore emailCodeStore;
    @Autowired
    private EmailSender emailSender;

    @Value("${shiguang.email-code.ttl:300000}")
    private long emailCodeTtlMs;
    @Value("${shiguang.dev-mode:true}")
    private boolean devMode;

    @Override
    public SendCodeVO sendEmailCode(String email) {
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new BusinessException("邮箱格式不正确");
        }
        // 已完成注册的邮箱不允许再走注册流程，避免用户误以为“再次注册”会覆盖原账号
        User activeUser = userMapper.getByEmail(email);
        if (activeUser != null && STATUS_ACTIVE.equals(activeUser.getStatus())) {
            throw new BusinessException("该邮箱已注册，请直接登录");
        }

        String code = String.format("%06d", ThreadLocalRandom.current().nextInt(1_000_000));
        // 存入 Redis：同一邮箱覆盖旧码，TTL 到期后自动失效
        emailCodeStore.save(email, code, emailCodeTtlMs);

        int ttlMinutes = (int) (emailCodeTtlMs / 1000 / 60);
        try {
            if (emailSender.isConfigured()) {
                // 真实发信：验证码通过 SMTP 送达用户邮箱
                emailSender.sendVerificationCode(email, code, ttlMinutes);
            } else if (devMode) {
                // 本地开发未配 SMTP：只打日志并回显，便于真机调试
                log.info("邮箱验证码（未发送邮件，仅开发模式） email={} code={}", email, code);
            } else {
                throw new BusinessException(500, "邮件服务未配置，无法发送验证码");
            }
        } catch (BusinessException ex) {
            // 发送失败时删掉刚存的验证码，避免留下不可达的“有效码”
            emailCodeStore.delete(email);
            throw ex;
        }
        return new SendCodeVO(true, devMode ? code : null);
    }

    @Override
    @Transactional
    public boolean verifyEmailForRegister(String email, String code) {
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new BusinessException("邮箱格式不正确");
        }
        if (code == null || code.length() != 6) {
            throw new BusinessException("验证码格式不正确");
        }

        verifyEmailCode(email, code);

        User pending = userMapper.getByEmail(email);
        if (pending == null) {
            // pending 用户在此刻才创建：只有验证码校验通过的用户才会占用邮箱，
            // 避免恶意提交大量未验证的占位账号。
            pending = createPendingUser(email);
        } else if (STATUS_ACTIVE.equals(pending.getStatus())) {
            throw new BusinessException("该邮箱已注册，请直接登录");
        }
        return true;
    }

    @Override
    @Transactional
    public boolean completeRegister(String email, String password, String nickname) {
        if (password == null || password.length() < PASSWORD_MIN_LENGTH) {
            throw new BusinessException("密码至少需要 6 位");
        }
        User user = userMapper.getByEmail(email);
        if (user == null || !STATUS_PENDING.equals(user.getStatus())) {
            throw new BusinessException("请先完成邮箱验证");
        }

        user.setPasswordHash(PasswordUtil.hash(password));
        // 昵称允许留空；为空时用邮箱前缀兜底，避免后续页面出现空昵称。
        String finalNickname = nickname == null || nickname.isBlank()
                ? email.substring(0, email.indexOf('@'))
                : nickname.trim();
        user.setNickname(finalNickname);
        user.setStatus(STATUS_ACTIVE);
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.completeRegistration(user);
        return true;
    }

    @Override
    public LoginVO loginByEmail(String email, String password) {
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new BusinessException(401, "邮箱或密码错误");
        }
        if (password == null || password.isEmpty()) {
            throw new BusinessException(401, "邮箱或密码错误");
        }

        User user = userMapper.getByEmail(email);
        // 统一提示“邮箱或密码错误”，避免通过接口探测邮箱是否已注册
        if (user == null || !STATUS_ACTIVE.equals(user.getStatus())
                || !PasswordUtil.matches(password, user.getPasswordHash())) {
            throw new BusinessException(401, "当前邮箱未注册");
        }
        return buildLoginVO(user);
    }

    @Override
    @Transactional
    public LoginVO socialLogin(String channel, String code) {
        if (!SOCIAL_CHANNELS.contains(channel)) {
            throw new BusinessException("不支持的登录渠道: " + channel);
        }

        // 开发模式：拿不到真实第三方授权结果，统一使用 mock uid；
        // 接入微信/抖音/苹果正式 SDK 后，这里替换为“调用开放平台换取 openid”。
        String channelUid = devMode ? "mock-" + channel : buildRealChannelUid(channel, code);

        AuthAccount authAccount = authAccountMapper.getByChannelAndUid(channel, channelUid);
        if (authAccount != null) {
            User user = userMapper.getById(authAccount.getUserId());
            if (user != null && STATUS_ACTIVE.equals(user.getStatus())) {
                return buildLoginVO(user);
            }
        }

        String nickname = switch (channel) {
            case "wechat" -> "微信用户";
            case "douyin" -> "抖音用户";
            case "apple" -> "Apple 用户";
            default -> "拾光用户";
        };
        User user = createSocialUser(nickname);
        AuthAccount bind = new AuthAccount();
        bind.setUserId(user.getId());
        bind.setChannel(channel);
        bind.setChannelUid(channelUid);
        bind.setCreatedAt(LocalDateTime.now());
        authAccountMapper.insert(bind);
        return buildLoginVO(user);
    }

    @Override
    public UserInfoVO getCurrentUser(String userId) {
        User user = userMapper.getById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        return toUserInfoVO(user);
    }



    /** 校验验证码：开发模式额外允许固定码 123456，见类注释说明。 */
    private void verifyEmailCode(String email, String code) {
        if (devMode && DEV_CODE.equals(code)) {
            return;
        }
        String savedCode = emailCodeStore.get(email);
        if (savedCode == null || !savedCode.equals(code)) {
            throw new BusinessException(400, "验证码错误或已过期");
        }
        // 验证码是一次性的：使用后立即删除，防止同一验证码被重复利用
        emailCodeStore.delete(email);
    }

    /** 注册第一步通过后创建待激活账号（无密码，status=pending）。 */
    private User createPendingUser(String email) {
        LocalDateTime now = LocalDateTime.now();
        User user = new User();
        user.setId("u-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12));
        user.setEmail(email);
        user.setNickname("");
        user.setStatus(STATUS_PENDING);
        user.setBio("");
        user.setTagline("");
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        userMapper.insert(user);
        return user;
    }

    /** 第三方登录新用户直接创建为可用账号。 */
    private User createSocialUser(String nickname) {
        LocalDateTime now = LocalDateTime.now();
        User user = new User();
        user.setId("u-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12));
        user.setEmail(null);
        user.setNickname(nickname);
        user.setStatus(STATUS_ACTIVE);
        user.setBio("");
        user.setTagline("");
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        userMapper.insert(user);
        return user;
    }

    private LoginVO buildLoginVO(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, user.getId());
        String token = JwtUtil.createJWT(jwtProperties.getSecretKey(), jwtProperties.getTokenTtl(), claims);
        return LoginVO.builder().token(token).user(toUserInfoVO(user)).build();
    }

    private UserInfoVO toUserInfoVO(User user) {
        return UserInfoVO.builder()
                .id(user.getId())
                .nickname(user.getNickname())
                .avatarUrl(user.getAvatarUrl())
                .bio(user.getBio())
                .tagline(user.getTagline())
                .build();
    }

    /**
     * 模拟真实第三方换取结果。
     *
     * 由于没有真实开放平台凭据，这里用 code 的摘要作为渠道内 uid；
     * 一旦接入正式 SDK，本方法应替换为对应开放平台的换取逻辑。
     */
    private String buildRealChannelUid(String channel, String code) {
        String raw = code == null ? "anonymous" : code;
        return channel + "-" + Math.abs(raw.hashCode());
    }
}
