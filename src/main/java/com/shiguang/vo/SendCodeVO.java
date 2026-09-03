package com.shiguang.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 发送验证码结果。
 *
 * devCode 仅开发模式返回，方便真机调试时直接在响应里看到验证码；
 * 生产环境该字段恒为 null。
 */
@Data
@AllArgsConstructor
@Schema(description = "发送邮箱验证码结果")
public class SendCodeVO {

    @Schema(description = "是否已受理（true）", example = "true")
    private boolean ok;

    @Schema(description = "开发模式回显的验证码，方便真机调试；生产环境恒为 null", example = "123456", nullable = true)
    private String devCode;
}
