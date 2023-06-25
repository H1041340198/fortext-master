package com.nplat.convert.config;

/**
 * 服务异常，Controller可直接抛出ServiceException，由ServiceExceptionHandler异常处理切面处理
 */
public class ServiceException extends Exception {

    public ServiceException(String s) {
        super(s);
    }

}
