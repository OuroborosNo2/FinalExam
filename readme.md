# 项目管控平台客户端依赖
### 说明
本依赖基于Spring、SpringMvc框架开发
### 更新情况
* 2022.8.12 发布0.0.1版本.
     * 存在问题:
          1. 日志输出到文件功能未完成
          2. 监控数据的传输是单线程单socket串行传输,无法处理大批量请求
### 使用教程
```
<dependency>
     <groupId>com.qgstudio</groupId>
     <artifactId>monitor</artifactId>
     <version>0.0.1</version>
</dependency>
```

导包后需要添加拦截器
```
@Configuration
public class SpringMvcSupport extends WebMvcConfigurationSupport {

    @Autowired
    private MonitorInterceptor monitorInterceptor;
    @Autowired
    private MonitorConfig monitorConfig;

    @Override
    protected void addInterceptors(InterceptorRegistry registry) {
        //配置拦截器,要监控的接口
        registry.addInterceptor(monitorInterceptor).addPathPatterns(monitorConfig.getPathPatterns());
    }
}

```
可选配置,添加在
```
spring:
  monitor:
    #监控数据流向的服务器地址,默认为localhost
    host: 39.98.41.126
    #监控数据流向的服务器地址端口，默认为8080
    port: 31101
    #是否将日志输出到控制台，默认为true
    log-to-console: false
    #是否将日志输出到文件，默认为true
    log-to-file: true
    #日志输出的文件路径，默认为../monitorLogs
    log-file-path: "../monitorLogs"
    #需要监听的接口，默认为'/**'
    path-patterns: "/**"
    #本项目的地址/主页，需要作为标识向服务端提供身份
    project-url: "106.13.18.48"
```


可选注解
```
#自定义特征(字段),一并监控,传输给服务端
@CustomTrait
```
