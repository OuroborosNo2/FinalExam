package com.qgstudio.util;

import com.qgstudio.annotation.CustomTrait;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * @program: monitor
 * @description: 获取被注解的字段
 * @author: ouroborosno2
 * @create: 2022-08-10
 **/
@Slf4j
public class FieldsUtil {
    public static List<Field> getAnnotatedFields(Class<?> clazz, Class annotationClazz) {
        Field[] declaredFields = clazz.getDeclaredFields();
        List<Field> result = new ArrayList<>();
        for (Field f : declaredFields) {
            //如果该字段被annotationClazz注解了
            if(null != f.getAnnotation(annotationClazz)){
                result.add(f);
            }
        }
        return result;
    }
    public static List<String> getFieldValues(Object obj,List<Field> fields){
        List<String> result = new ArrayList<>();
        fields.forEach((f)->{
            int modifiers = f.getModifiers();
            if(!Modifier.isStatic(modifiers)){
                throw new IllegalArgumentException(f.getName() + "字段必须修饰为static,否则@CustomTrait无法生效");
            }
            //强制设为可访问,无视private
            f.setAccessible(true);
            try {
                String name = f.getAnnotation(CustomTrait.class).value();
                if("".equals(name)){
                    name = f.getName();
                }
                result.add(name + ":" + f.get(obj).toString());

            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });
        return result;
    }

}
