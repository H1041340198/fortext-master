package com.nplat.convert.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ValidationException;

@Slf4j
@RestControllerAdvice
public class ExceptionHandlers {


    @ExceptionHandler(value = {HttpMessageConversionException.class, ValidationException.class,
            MethodArgumentNotValidException.class, TypeMismatchException.class})
    public BaseResponse requestParamErrorHandler(Exception e) {
        BaseResponse response = new BaseResponse();
        log.error("参数校验出现异常：", e);
        response.setMsgEnum(ApiMsgEnum.UNAUTHORIZED);
        response.setData(e.getMessage());
        return response;
    }

    @ExceptionHandler(value = Exception.class)
    public BaseResponse defaultErrorHandler(Exception e) {
        // 异常堆栈转字符串
        BaseResponse response = new BaseResponse();
        response.setMsgEnum(ApiMsgEnum.SERVICE_UNAVAILABLE);
        e.printStackTrace();
        response.setData(e.getMessage());
        return response;
    }
    @ExceptionHandler(value = UserNotFoundException.class)
    public BaseResponse userNotFoundExceptionHandler(Exception e) {
        log.error("用户未找到异常：", e);
        BaseResponse response = new BaseResponse();
        response.setMsgEnum(ApiMsgEnum.USER_NOT_EXISTS);
        response.setMessage(e.getMessage());
        return response;
    }

    @ExceptionHandler(value = UserTokenException.class)
    public BaseResponse userTokenExceptionHandler(Exception e) {
        log.error("token过期异常：", e);
        BaseResponse response = new BaseResponse();
        response.setMsgEnum(ApiMsgEnum.TOKEN_EXPIRED);
        response.setMessage(e.getMessage());
        return response;
    }

    @ExceptionHandler(value = ServiceException.class)
    public BaseResponse serviceExceptionHandler(Exception e) {
        log.error("ServiceException：", e);
        BaseResponse response = new BaseResponse();
        response.setMsgEnum(ApiMsgEnum.INTERNAL_SERVER_ERROR);
        response.setMessage(e.getMessage());
        return response;
    }





//    @Controller
//    public static class NotFoundException implements ErrorController {
//
//        @Override
//        public String getErrorPath() {
//            return "/root/web_www/www";
//        }
//
//
//    }
}