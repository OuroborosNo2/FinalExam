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
    private Socket sock;
    private OutputStream out;
    private PrintWriter pw;
    private String json;

    public SocketThread(MonitorConfig monitorConfig){
        try {
            sock = new Socket("39.98.41.126", 31101);
            out = sock.getOutputStream();
            pw = new PrintWriter(out);
        } catch (IOException e) {
            log.warn("连接远程服务器失败");
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        Thread.currentThread().setName("socket线程");
        while (true){
            synchronized (this) {
                log.warn("快生成文件!");
                //log.info("准备发送\n"+json);
                pw.write(json);
                pw.flush();
                //log.info("监控数据发送成功");
                try {
                    wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public void setJson(String json){
        this.json = json;
    }
}
