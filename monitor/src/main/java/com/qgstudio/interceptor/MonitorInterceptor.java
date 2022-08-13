package com.qgstudio.interceptor;

import com.alibaba.fastjson.JSON;
import com.qgstudio.config.MonitorConfig;
import com.qgstudio.SocketThread;
import com.qgstudio.annotation.CustomTrait;
import com.qgstudio.util.FieldsUtil;
import com.qgstudio.util.NetworkUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @program: monitor
 * @description: 监控拦截器
 * @author: ouroborosno2
 * @create: 2022-08-10
 **/
@Component
@Slf4j
public class MonitorInterceptor implements HandlerInterceptor {
    @Autowired
    private MonitorConfig monitorConfig;
    private static SocketThread socketThread;

    public MonitorInterceptor(){}

    /**访问者的ip*/
    private String ip;
    /**调用的方法*/
    private Method method;
    /**调用的方法的参数,如果真的传入了文件流,应该会出异常*/
    private Map<String, String> inParameters;
    /**调用的方法的返回值*/
    private Object outParameters;
    /**调用的方法所在的包*/
    private String packageName;
    /**请求的uri*/
    private String uri;
    /**用户自定义的特征值*/
    private List<String> traits;
    /**调用方法可能产生的异常*/
    private String exception;
    /**请求时间*/
    private LocalDateTime visitDate;
    /**请求开始*/
    private Instant startTime;
    /**请求结束*/
    private Instant endTime;
    /**请求耗费时间*/
    private long responseTime;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //如果是SpringMVC请求
        if(handler instanceof HandlerMethod){
            visitDate = LocalDateTime.now();

            HandlerMethod handlerMethod = (HandlerMethod) handler;

            String header = request.getHeader("Content-Type");
            String[] headers = new String[0];
            if(header != null) {
                headers = header.split(";");
            }

            //获取调用详情
            ip = NetworkUtil.getIpAddress(request);
            method = handlerMethod.getMethod();

            boolean flag = true;
            for (String s : headers) {
                if ("multipart/form-data".equals(s)){
                    //表单很可能会带文件,不处理body
                    flag = false;
                }
            }
            //不确定传参到底以何形式,先获取body
            if(flag){
                MyHttpServletRequestWrapper requestWrapper = new MyHttpServletRequestWrapper(request);
                String body = requestWrapper.getBody();
                inParameters = JSON.parseObject(body, Map.class);
            }
            //如果body为空再获取普通参数
            if(null == inParameters){
                inParameters = new HashMap<>();
                request.getParameterMap().forEach((key,value)->{
                    //<String,String[]>转<String,String>
                    inParameters.put(key, Arrays.toString(value));
                });
            }
            //TODO url上的参数

            packageName = handlerMethod.getBeanType().getName();
            uri = request.getRequestURI();
            //输出日志到控制台
            if(monitorConfig.isLogToConsole()) {
                log.info("---------------{}拦截了请求---------------",visitDate);
                log.info("当前拦截的ip为：{}", ip);
                log.info("当前拦截的方法为：{}", method.getName());
                log.info("当前拦截的方法参数数量为：{}", inParameters.size());
                inParameters.forEach((key, value) -> {
                    log.info("{}:{}", key, value);
                });
                log.info("当前拦截的方法所属类为：{}", packageName);
                log.info("当前拦截的请求的uri为：{}", uri);
            }
            //记录方法执行的开始时间
            startTime = Instant.now();
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //获取结束时间
        endTime = Instant.now();
        //计算方法调用用时
        Duration duration = Duration.between(startTime,endTime);
        responseTime = duration.toMillis();
        //获取出参
        outParameters = request.getSession().getAttribute("bodyForMonitor");

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        //获取被CustomTrait注解的字段
        List<Field> fields = FieldsUtil.getAnnotatedFields(handlerMethod.getBeanType(), CustomTrait.class);
        //获得bean实例
        Object bean = handlerMethod.getBean();
        traits = FieldsUtil.getFieldValues(bean,fields);

        //输出日志到控制台
        if(monitorConfig.isLogToConsole()) {
            log.info("此次请求的返回值为：{}", outParameters);
            log.info("自定义特征为：{}", traits);
            //异常日志
            if (null != ex) {
                StringWriter sw = new StringWriter();
                ex.printStackTrace(new PrintWriter(sw, true));
                exception = sw.toString();
                log.info(exception);
            }
            log.info("----------------此次请求花费的时间：{}ms----------------", responseTime);
        }

        Map<String,Object> map = new HashMap<>();

        map.put("projectUrl",monitorConfig.getProjectUrl());
        map.put("ip",ip);
        map.put("method",method.getName());
        map.put("inParameters",inParameters);
        map.put("outParameters",outParameters);
        map.put("packageName",packageName);
        map.put("uri",uri);
        map.put("traits",traits);
        map.put("exception",exception);
        map.put("visitDate",visitDate);
        map.put("responseTime",responseTime);
        String json = JSON.toJSONString(map);

        if(socketThread == null){
            socketThread = new SocketThread(monitorConfig);
        }
        if(!socketThread.isAlive()){
            socketThread.setJson(json);
            socketThread.start();
        }else{
            synchronized (socketThread) {
                socketThread.setJson(json);
                socketThread.notify();
            }
        }
    }
}
