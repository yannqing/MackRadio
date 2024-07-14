package com.yannqing.mackradio.exception;

import com.yannqing.mackradio.common.Code;
import com.yannqing.mackradio.utils.ResultUtils;
import com.yannqing.mackradio.vo.BaseResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;
import java.nio.file.AccessDeniedException;

/**
 * 全局异常处理器
 *
 * @author yannqing
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    /**
     * 权限校验异常
     */
    @ExceptionHandler(AccessDeniedException.class)
    public BaseResponse<Object> handleAccessDeniedException(AccessDeniedException e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("请求地址 {},权限校验失败 {}", requestURI, e.getMessage());
        return ResultUtils.failure("没有权限，请联系管理员授权");
    }

    /**
     * 请求方式不支持
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public BaseResponse<Object> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException e,
                                                                    HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("请求地址 {},不支持 {} 请求", requestURI, e.getMethod());
        return ResultUtils.failure(e.getMessage());
    }

    /**
     * 拦截未知的运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    public void handleRuntimeException(RuntimeException e, HttpServletRequest request, HttpServletResponse response) throws IOException {
        String requestURI = request.getRequestURI();
        log.error("请求地址 {},异常: {}", requestURI, e);

        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(500);
        response.getWriter().write(ResultUtils.failure(e.getMessage()).toString());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public BaseResponse<Object> handleRedisConnectionFailureException(IllegalArgumentException e, HttpServletRequest request, HttpServletResponse response){
        log.error("参数错误：{}", e.getMessage());
        return ResultUtils.failure("参数错误->"+e.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public BaseResponse<Object> handleIllegalStateException(IllegalStateException e, HttpServletRequest request, HttpServletResponse response){
        log.error("认证失败：{}", e.getMessage());
        return ResultUtils.failure(Code.AUTHENTICATE_FAILURE, null, "认证失败: "+e.getMessage());
    }

    @ExceptionHandler(BusinessException.class)
    public BaseResponse businessExceptionHandler(BusinessException e) {
        log.error("businessException: " + e.getMessage(), e);
        return ResultUtils.failure(e.getCode(), e.getMessage());
    }

    /**
     * 系统异常
     */
    @ExceptionHandler(Exception.class)
    public BaseResponse<Object> handleException(Exception e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("请求地址 {},发生系统异常.", requestURI, e);
        return ResultUtils.failure(e.getMessage());
    }



}
