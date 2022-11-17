package com.gznx.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.gznx.response.CommonResp;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;

/**
 * 全局异常处理器
 *
 * @author xp
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 请求方式不支持
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public CommonResp handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException e,
                                                          HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        LOG.error("请求地址'{}',不支持'{}'请求", requestURI, e.getMethod());
        CommonResp resp = new CommonResp<>();
        resp.setSuccess(false);
        resp.setMessage(e.getMessage());
        return resp;
    }

    /**
     * 拦截未知的运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    public CommonResp handleRuntimeException(RuntimeException e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        LOG.error("请求地址'{}',发生未知异常.", requestURI, e);
        CommonResp resp = new CommonResp<>();
        resp.setSuccess(false);
        resp.setMessage(e.getMessage());
        return resp;
    }

    /**
     * 系统异常
     */
    @ExceptionHandler(Exception.class)
    public CommonResp handleException(Exception e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        LOG.error("请求地址'{}',发生系统异常.", requestURI, e);
        CommonResp resp = new CommonResp<>();
        resp.setSuccess(false);
        resp.setMessage(e.getMessage());
        return resp;
    }

    /**
     * 自定义验证异常
     */
    @ExceptionHandler(BindException.class)
    public CommonResp handleBindException(BindException e) {
        LOG.error(e.getMessage(), e);
        String message = e.getAllErrors().get(0).getDefaultMessage();
        CommonResp resp = new CommonResp<>();
        resp.setSuccess(false);
        resp.setMessage(message);
        return resp;
    }

    /**
     * 自定义验证异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Object handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        LOG.error(e.getMessage(), e);
        String message = e.getBindingResult().getFieldError().getDefaultMessage();
        CommonResp resp = new CommonResp<>();
        resp.setSuccess(false);
        resp.setMessage(message);
        return resp;
    }

}
