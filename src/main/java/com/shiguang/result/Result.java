package com.shiguang.result;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

/**
 * 统一接口返回结构。
 *
 * 采用与 sky-take-out 一致的 {code, msg, data} 包裹：
 * code=1 表示成功，其余为失败码（与 HTTP 状态码保持一致便于排查）。
 */
@Data
@Schema(description = "统一返回结构：{code, msg, data}，code=1 表示成功，其余为失败码（与 HTTP 状态码保持一致）")
public class Result<T> implements Serializable {

    @Schema(description = "业务码：1=成功；失败时为错误码（通常与 HTTP 状态码一致）", example = "1")
    private Integer code;

    @Schema(description = "提示信息；成功时为 success", example = "success")
    private String msg;

    @Schema(description = "业务数据；失败时为 null")
    private T data;

    public static <T> Result<T> success() {
        Result<T> result = new Result<>();
        result.code = 1;
        result.msg = "success";
        return result;
    }

    public static <T> Result<T> success(T object) {
        Result<T> result = success();
        result.data = object;
        return result;
    }

    public static <T> Result<T> error(String msg) {
        Result<T> result = new Result<>();
        result.code = 0;
        result.msg = msg;
        return result;
    }

    public static <T> Result<T> error(int code, String msg) {
        Result<T> result = new Result<>();
        result.code = code;
        result.msg = msg;
        return result;
    }
}
