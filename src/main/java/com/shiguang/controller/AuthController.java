package com.shiguang.controller;

import com.shiguang.dto.EmailLoginDTO;
import com.shiguang.dto.RegisterVerifyDTO;
import com.shiguang.dto.SendEmailCodeDTO;
import com.shiguang.dto.SetPasswordDTO;
import com.shiguang.dto.PasswordResetCompleteDTO;
import com.shiguang.dto.SocialLoginDTO;
import com.shiguang.result.Result;
import com.shiguang.service.AuthService;
import com.shiguang.vo.LoginVO;
import com.shiguang.vo.SendCodeVO;
import com.shiguang.vo.PasswordResetTokenVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证接口（无需登录）。
 *
 * 提供：
 * - 邮箱注册两步流程（验证码校验 -> 设置密码）；
 * - 邮箱 + 密码登录（默认登录方式）；
 * - 微信/抖音/Apple 第三方登录（开发模式 mock）。
 */
@RestController
@RequestMapping(value = "/api/auth", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "认证接口", description = "邮箱注册两步流程 / 邮箱密码登录 / 第三方登录")
@Slf4j
public class AuthController {

    @Autowired
    private AuthService authService;

    @Operation(summary = "发送邮箱验证码",
            description = "校验邮箱格式后发送 6 位验证码（Redis 存储，TTL 5 分钟，同邮箱重发会覆盖旧码）。"
                    + "已完成注册的邮箱不允许再次发送。开发模式下响应回显 devCode，且固定验证码 123456 可直接通过。")
    @PostMapping("/email-code")
    public Result<SendCodeVO> sendEmailCode(@RequestBody SendEmailCodeDTO dto) {
        log.info("发送邮箱验证码：email={}", dto.getEmail());
        return Result.success(authService.sendEmailCode(dto.getEmail()));
    }

    @Operation(summary = "发送密码重置验证码",
            description = "向已存在的邮箱账号发送验证码；无论邮箱是否存在均返回统一受理结果，避免账号枚举。")
    @PostMapping("/password-reset/email-code")
    public Result<SendCodeVO> sendPasswordResetCode(@RequestBody SendEmailCodeDTO dto) {
        log.info("发送密码重置验证码：email={}", dto.getEmail());
        return Result.success(authService.sendPasswordResetCode(dto.getEmail()));
    }

    @Operation(summary = "校验密码重置验证码")
    @PostMapping("/password-reset/verify")
    public Result<PasswordResetTokenVO> verifyPasswordResetCode(
            @RequestBody RegisterVerifyDTO dto) {
        log.info("校验密码重置验证码：email={}", dto.getEmail());
        return Result.success(authService.verifyPasswordResetCode(dto.getEmail(), dto.getCode()));
    }

    @Operation(summary = "完成密码重置")
    @PostMapping("/password-reset/complete")
    public Result<Boolean> completePasswordReset(@RequestBody PasswordResetCompleteDTO dto) {
        return Result.success(authService.completePasswordReset(dto.getResetToken(), dto.getPassword()));
    }

    @Operation(summary = "邮箱注册-校验验证码",
            description = "注册第一步：校验验证码并创建待激活账号（status=pending）。"
                    + "校验通过后该邮箱即被占用，随后调用 register/set-password 设置密码。"
                    + "开发模式下固定验证码 123456 可直接通过。")
    @PostMapping("/register/email-verify")
    public Result<Boolean> verifyEmailForRegister(@RequestBody RegisterVerifyDTO dto) {
        log.info("邮箱注册-校验验证码：email={}", dto.getEmail());
        return Result.success(authService.verifyEmailForRegister(dto.getEmail(), dto.getCode()));
    }

    @Operation(summary = "邮箱注册-设置密码",
            description = "注册第二步：为已通过邮箱验证的账号设置密码并激活（status=active）。"
                    + "密码至少 6 位；昵称可选，为空时自动取邮箱前缀。")
    @PostMapping("/register/set-password")
    public Result<Boolean> completeRegister(@RequestBody SetPasswordDTO dto) {
        log.info("邮箱注册-设置密码：email={}", dto.getEmail());
        return Result.success(authService.completeRegister(dto.getEmail(), dto.getPassword(), dto.getNickname()));
    }

    @Operation(summary = "邮箱密码登录",
            description = "邮箱 + 密码登录，成功返回 JWT token 与当前用户信息。"
                    + "邮箱不存在或密码错误时统一返回 401。")
    @PostMapping("/login")
    public Result<LoginVO> loginByEmail(@RequestBody EmailLoginDTO dto) {
        log.info("邮箱密码登录：email={}", dto.getEmail());
        return Result.success(authService.loginByEmail(dto.getEmail(), dto.getPassword()));
    }

    @Operation(summary = "第三方登录（微信/抖音/Apple）",
            description = "channel 支持 wechat / douyin / apple。"
                    + "苹果登录传 identityToken，微信/抖音传 code；开发模式为 mock 登录，"
                    + "任意非空凭据均可通过（生产环境需接入开放平台换取真实身份）。")
    @PostMapping("/login/{channel}")
    public Result<LoginVO> socialLogin(
            @Parameter(description = "第三方渠道：wechat / douyin / apple", example = "wechat")
            @PathVariable String channel,
                                       @RequestBody(required = false) SocialLoginDTO dto) {
        String code = dto == null ? null : dto.getCode();
        if (code == null && dto != null) {
            code = dto.getIdentityToken();
        }
        log.info("第三方登录：channel={}", channel);
        return Result.success(authService.socialLogin(channel, code));
    }
}
