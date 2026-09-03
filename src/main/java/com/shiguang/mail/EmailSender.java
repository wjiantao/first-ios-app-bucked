package com.shiguang.mail;

import com.shiguang.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;

/**
 * 注册验证码邮件发送器。
 *
 * 基于 spring.mail.* 的 SMTP 配置；未配置 spring.mail.host 时
 * Spring 不会创建 JavaMailSender（{@link #isConfigured()} 返回 false），
 * 调用方应回退到开发模式日志，或直接报“邮件服务未配置”。
 */
@Component
@Slf4j
public class EmailSender {

    private static final String SENDER_NAME = "拾光";

    private final JavaMailSender mailSender;
    private final String host;
    private final String from;

    public EmailSender(
            @Autowired(required = false) JavaMailSender mailSender,
            @Value("${spring.mail.host:}") String host,
            @Value("${shiguang.mail.from:}") String from,
            @Value("${spring.mail.username:}") String username) {
        this.mailSender = mailSender;
        this.host = host;
        // 发件人优先用 shiguang.mail.from，未配置则退回 SMTP 登录账号
        this.from = from == null || from.isBlank() ? username : from;
    }

    /** SMTP 是否已配置（spring.mail.host 非空才有 JavaMailSender）。 */
    public boolean isConfigured() {
        return mailSender != null && host != null && !host.isBlank();
    }

    /** 发送注册验证码邮件。 */
    public void sendVerificationCode(String email, String code, int ttlMinutes) {
        if (!isConfigured()) {
            throw new BusinessException(500, "邮件服务未配置，无法发送验证码");
        }
        if (from == null || from.isBlank()) {
            throw new BusinessException(500, "邮件发件人未配置，无法发送验证码");
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from, SENDER_NAME);
            helper.setTo(email);
            helper.setSubject("【拾光】注册验证码");
            helper.setText(buildBody(code, ttlMinutes));
            mailSender.send(message);
            log.info("验证码邮件发送成功 email={}", email);
        } catch (MessagingException | UnsupportedEncodingException | MailException ex) {
            log.error("验证码邮件发送失败 email={}", email, ex);
            throw new BusinessException(500, "验证码邮件发送失败，请稍后重试");
        }
    }

    private String buildBody(String code, int ttlMinutes) {
        return "你正在注册「拾光」。\n\n"
                + "你的验证码是：" + code + "\n\n"
                + "验证码 " + ttlMinutes + " 分钟内有效，请勿泄露给他人。\n"
                + "如非本人操作，请忽略本邮件。";
    }
}
