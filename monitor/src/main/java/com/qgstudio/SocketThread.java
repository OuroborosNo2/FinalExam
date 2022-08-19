package com.qgstudio;

import com.qgstudio.config.MonitorConfig;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * @program: monitor
 * @description: socket线程，发送监控数据，目前是单线程串行发送，可能无法处理大批量请求
 * @author: ouroborosno2
 * @create: 2022-08-10
 **/
@Slf4j
public class SocketThread extends Thread{
    String host;
    int port;
    private Socket sock;
    private OutputStream out;
    private PrintWriter pw;
    private String json;

    public SocketThread(MonitorConfig monitorConfig){
        host = monitorConfig.getHost();
        port = monitorConfig.getPort();
        //openSocket();
    }

    @Override
    public void run() {
        Thread.currentThread().setName("socket线程");
        while (true){
            synchronized (this) {
                openSocket();
                /*
                心跳包不起作用，可能是因为服务端是netty
                try {
                    //发送心跳包，如果服务端Socket的SO_OOBINLINE属性没有打开，就会自动舍弃这个字节
                    sock.sendUrgentData(0xFF);
                } catch (IOException e) {
                    //如果连接已经断开，会抛出异常
                    //重新连接
                    log.info("socket连接已断开，正在重连");
                    openSocket();
                }*/
                log.info("准备发送监控数据:"+json);
                pw.write(json);
                pw.flush();
                log.info("监控数据发送成功");
                try {
                    pw.close();
                    out.close();
                    sock.close();
                    log.info("断开连接");
                } catch (IOException e) {
                    log.warn("断开连接出错");
                    throw new RuntimeException(e);
                }
                try {
                    wait();
                } catch (InterruptedException e) {
                    log.info("监控数据发送失败");
                    throw new RuntimeException(e);
                }
            }
        }
    }
    public void setJson(String json){
        this.json = json;
    }
    public void openSocket(){
        try {
            log.info("正在连接远程服务器{}:{}",host,port);
            sock = new Socket(host, port);
            //设置超时时间 60s
            sock.setSoTimeout(6000);
            out = sock.getOutputStream();
            pw = new PrintWriter(out);
            if(sock.isConnected()){
                log.info("连接成功");
            }
        } catch (IOException e) {
            log.warn("连接远程服务器{}:{}失败",host,port);
            throw new RuntimeException(e);
        }
    }
}
