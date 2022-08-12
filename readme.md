# 项目管控平台客户端依赖
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
