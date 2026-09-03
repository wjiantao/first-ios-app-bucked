package com.shiguang.handler;

import com.shiguang.exception.BusinessException;
import com.shiguang.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器。
 *
 * 业务异常返回其自带的状态码与提示；未预期的异常统一返回 500，
 * 避免把堆栈或数据库细节直接暴露给 RN 客户端。
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<Object>> handleBusinessException(BusinessException ex) {
        log.warn("业务异常：{}", ex.getMessage());
        return ResponseEntity
                .status(ex.getHttpStatus())
                .body(Result.error(ex.getCode(), ex.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Result<Object>> handleMessageNotReadable(HttpMessageNotReadableException ex) {
        log.warn("请求体解析失败：{}", ex.getMessage());
        return ResponseEntity.badRequest().body(Result.error("请求体缺失或格式错误"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Object>> handleException(Exception ex) {
        log.error("系统异常", ex);
        return ResponseEntity
                .status(500)
                .body(Result.error(500, "服务器繁忙，请稍后重试"));
    }
}
