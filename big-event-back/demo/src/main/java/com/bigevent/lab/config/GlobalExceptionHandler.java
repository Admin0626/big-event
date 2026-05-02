package com.bigevent.lab.config;

import com.bigevent.lab.common.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 * 统一捕获参数校验异常、业务异常和其他未知异常
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理 @Validated 参数校验异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidationException(MethodArgumentNotValidException e) {
        String errorMsg = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(fieldError -> fieldError.getDefaultMessage())
                .orElse("参数校验失败");
        return Result.error(errorMsg);
    }

    /**
     * 处理HTTP 401未授权状态
     */
    @ExceptionHandler(jakarta.servlet.ServletException.class)
    public Result<Void> handleServletException(jakarta.servlet.ServletException e, HttpServletRequest request, HttpServletResponse response) {
        if (response.getStatus() == HttpServletResponse.SC_UNAUTHORIZED) {
            return Result.error("未登录或登录已过期");
        }
        return Result.error(e.getMessage());
    }

    /**
     * 处理其他未知异常
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常: ", e);
        return Result.error("系统异常，请联系管理员");
    }
}
