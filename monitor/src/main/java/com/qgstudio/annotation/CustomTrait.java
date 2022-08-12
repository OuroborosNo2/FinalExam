package com.qgstudio.annotation;

import com.qgstudio.util.FieldsUtil;

import javax.security.auth.login.Configuration;
import java.lang.annotation.*;

/**
 * @program: monitor
 * @description: 用于监控自定义特征,被注解的字段必须为static，否则获取不了
 * @author: ouroborosno2
 * @create: 2022-08-10
 **/
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CustomTrait {
    /**所监控的自定义特征的名称*/
    String value() default "";
}
