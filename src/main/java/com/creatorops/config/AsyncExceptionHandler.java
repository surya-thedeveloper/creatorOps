package com.creatorops.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import java.lang.reflect.Method;

/**
 * Handles exceptions thrown during the execution of asynchronous tasks where the caller 
 * cannot intercept the exception directly (e.g. methods returning void).
 */
public class AsyncExceptionHandler implements AsyncUncaughtExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(AsyncExceptionHandler.class);

    @Override
    public void handleUncaughtException(Throwable ex, Method method, Object... params) {
        log.error("Unhandled exception caught in asynchronous execution context of method '{}' with parameters: {}", 
                method.getName(), params, ex);
    }
}
