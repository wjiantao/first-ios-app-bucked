package com.shiguang.store;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * 密码重置凭证的 Redis 存储。
 *
 * 凭证只保存邮箱，不保存密码；完成重置时删除凭证，配合 TTL 保证短期且一次性使用。
 */
@Component
public class PasswordResetTokenStore {

    private static final String KEY_PREFIX = "password:reset:token:";
    private final StringRedisTemplate redisTemplate;

    public PasswordResetTokenStore(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /** 保存重置凭证。 */
    public void save(String token, String email, long ttlMs) {
        redisTemplate.opsForValue().set(KEY_PREFIX + token, email, Duration.ofMillis(ttlMs));
    }

    /** 读取凭证对应的邮箱；不存在或过期时返回 null。 */
    public String get(String token) {
        return redisTemplate.opsForValue().get(KEY_PREFIX + token);
    }

    /** 消费凭证，防止同一凭证重复修改密码。 */
    public void delete(String token) {
        redisTemplate.delete(KEY_PREFIX + token);
    }
}
