package com.nplat.convert.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;

import java.lang.reflect.Method;

@Slf4j
public class GlobalAsyncUncaughtExceptionHandler implements AsyncUncaughtExceptionHandler {

    public GlobalAsyncUncaughtExceptionHandler() {
    }

    public void handleUncaughtException(Throwable ex, Method method, Object... params) {
        log.error("GlobalAsyncUncaughtExceptionHandler 捕捉到异常,发送到 WMonitor", ex);
//        WMonitor.sum(55197, 1);


    }
}