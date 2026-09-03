package com.shiguang.store;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * 邮箱验证码的 Redis 存储。
 *
 * 键为 email:code:{email}，同一邮箱再次发送时直接覆盖旧码；
 * 过期时间由 Redis TTL 保证，验证通过后手动删除保证一次性使用。
 */
@Component
public class EmailCodeStore {

    private static final String KEY_PREFIX = "email:code:";

    private final StringRedisTemplate redisTemplate;

    public EmailCodeStore(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /** 保存验证码，ttlMs 毫秒后自动过期。 */
    public void save(String email, String code, long ttlMs) {
        redisTemplate.opsForValue().set(KEY_PREFIX + email, code, Duration.ofMillis(ttlMs));
    }

    /** 取出验证码；不存在或已过期返回 null。 */
    public String get(String email) {
        return redisTemplate.opsForValue().get(KEY_PREFIX + email);
    }

    /** 删除验证码（验证通过后调用，确保一次性使用）。 */
    public void delete(String email) {
        redisTemplate.delete(KEY_PREFIX + email);
    }
}
