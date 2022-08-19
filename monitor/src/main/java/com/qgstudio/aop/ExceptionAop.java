package com.qgstudio.aop;

import com.qgstudio.interceptor.MonitorInterceptor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * 如果所监控的项目有进行全局异常捕获，拦截器的afterCompletion将捕获不了异常，故在此情况为其异常捕获器增加切面截取其异常
 */
@Component
@Aspect
public class ExceptionAop {
    @Pointcut("@annotation(org.springframework.web.bind.annotation.ExceptionHandler)")
    public void exceptionHandler() {
    }

    @Around("exceptionHandler()")
    public Object getException(ProceedingJoinPoint pjp) throws Throwable {
        Object[] args = pjp.getArgs();
        Exception e = (Exception) args[0];
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw, true));
        String exception = sw.toString();
        //堆栈信息太长了，每次都有大量相同内容，所以截取掉反射调用之后的内容
        try {
            exception = exception.substring(0, exception.indexOf("at sun.reflect"));
        }catch (Exception ex){}
        MonitorInterceptor.exceptionContainer.set(exception);
        return pjp.proceed(args);
    }

}
