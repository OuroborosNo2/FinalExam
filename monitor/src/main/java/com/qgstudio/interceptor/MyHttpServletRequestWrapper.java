package com.qgstudio.interceptor;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
/**
 * @program: monitor
 * @description: 手动对请求封装进行修改,多次获取inputStream
 * @author: ouroborosno2
 * @create: 2022-08-10
 **/
public class MyHttpServletRequestWrapper extends HttpServletRequestWrapper {

    private final String body;

    public MyHttpServletRequestWrapper(HttpServletRequest request) throws IOException {
        super(request);
        InputStream is = null;
        StringBuilder sb = null;
        try {
            //分段读取请求体
            is = request.getInputStream();
            sb = new StringBuilder();
            byte[] b = new byte[4096];
            for (int n ; (n = is.read(b)) != -1;)
            {
                sb.append(new String(b, 0, n));
            }
        } finally {
            if(is != null) {
                is.close();
            }
        }
        body = sb.toString();
    }

    /**request.getInputStream()只能获取一次,自己获取了一次,必须重写*/
    @Override
    public ServletInputStream getInputStream() throws IOException {
        final ByteArrayInputStream bais = new ByteArrayInputStream(body.getBytes());
        return new ServletInputStream() {

            @Override
            public boolean isFinished() {
                return false;
            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setReadListener(ReadListener readListener) {

            }
            @Override
            public int read() throws IOException {

                return bais.read();

            }

            @Override
            public void close() throws IOException {
                bais.close();
            }
        };
    }

    public String getBody() {
        return body;
    }
}