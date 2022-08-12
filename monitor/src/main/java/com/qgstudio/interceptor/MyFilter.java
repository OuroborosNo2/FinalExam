package com.qgstudio.interceptor;

import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
/**
 * @program: monitor
 * @description: 对请求体进行处理
 * @author: ouroborosno2
 * @create: 2022-08-10
 **/
@Component
public class MyFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException, IOException {
        ServletRequest requestWrapper = null;
        String header = ((HttpServletRequest) servletRequest).getHeader("Content-Type");
        if(header != null && "multipart/form-data".equals(header.split(";")[0])) {
            //文件流不处理
        }else  {
            requestWrapper = new MyHttpServletRequestWrapper((HttpServletRequest) servletRequest);
        }
        if (requestWrapper == null) {
            filterChain.doFilter(servletRequest, servletResponse);
        }else {
            filterChain.doFilter(requestWrapper,servletResponse);
        }
    }

    @Override
    public void destroy() {
    }
}