package com.shiguang.exception;

import lombok.Getter;

/**
 * 业务异常。
 *
 * 同时携带业务码与 HTTP 状态码：
 * - code 会原样返回给客户端（如 401 未登录、404 不存在）；
 * - httpStatus 决定 HTTP 响应状态，便于 RN 端 axios 按状态码统一处理。
 */
@Getter
public class BusinessException extends RuntimeException {

    private final int code;
    private final int httpStatus;

    public BusinessException(String message) {
        this(0, 400, message);
    }

    public BusinessException(int httpStatus, String message) {
        this(httpStatus, httpStatus, message);
    }

    public BusinessException(int code, int httpStatus, String message) {
        super(message);
        this.code = code;
        this.httpStatus = httpStatus;
    }
}
