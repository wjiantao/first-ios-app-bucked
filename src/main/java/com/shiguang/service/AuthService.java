package com.shiguang.service;

import com.shiguang.vo.LoginVO;
import com.shiguang.vo.SendCodeVO;
import com.shiguang.vo.UserInfoVO;
import com.shiguang.vo.PasswordResetTokenVO;

/**
 * 认证服务：邮箱验证码注册、邮箱密码登录、第三方登录、当前用户查询。
 */
public interface AuthService {

    /** 发送邮箱注册验证码。 */
    SendCodeVO sendEmailCode(String email);

    /** 发送密码重置验证码；不存在的邮箱也返回统一结果，避免账号枚举。 */
    SendCodeVO sendPasswordResetCode(String email);

    /** 校验密码重置验证码并签发一次性短期凭证。 */
    PasswordResetTokenVO verifyPasswordResetCode(String email, String code);

    /** 使用一次性凭证更新邮箱账号密码。 */
    boolean completePasswordReset(String resetToken, String password);

    /** 注册第一步：校验验证码并创建待激活账号（pending）。 */
    boolean verifyEmailForRegister(String email, String code);

    /** 注册第二步：为已通过邮箱验证的账号设置密码并激活。 */
    boolean completeRegister(String email, String password, String nickname);

    /** 邮箱 + 密码登录。 */
    LoginVO loginByEmail(String email, String password);

    LoginVO socialLogin(String channel, String code);

    UserInfoVO getCurrentUser(String userId);


}
