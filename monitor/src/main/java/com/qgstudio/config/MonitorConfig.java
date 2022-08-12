package com.qgstudio.config;

import lombok.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
/**
 * @program: monitor
 * @description:
 * @author: ouroborosno2
 * @create: 2022-08-10
 **/
@Component
@ConfigurationProperties(prefix = "spring.monitor")
@Setter
@Getter
public class MonitorConfig {
    /**监控数据流向的服务器地址,默认为localhost*/
    @Builder.Default
    private String host = "localhost";
    /**监控数据流向的服务器地址端口，默认为8080*/
    @Builder.Default
    private int port = 8080;
    /**是否将日志输出到控制台，默认为true*/
    @Builder.Default
    private boolean logToConsole = true;
    /**是否将日志输出到文件，默认为true*/
    @Builder.Default
    private boolean logToFile = true;
    /**是日志输出的文件路径，默认为../monitorLogs*/
    @Builder.Default
    private String logFilePath = "../monitorLogs";
    /**需要监听的接口，默认为'/**'*/
    @Builder.Default
    private String pathPatterns = "/**";
    /**本项目的地址/主页，需要作为标识向服务端提供身份*/
    private String projectUrl;

}
