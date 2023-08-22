package com.crumb.definition;

import com.crumb.annotation.Component;
import com.crumb.web.Controller;
import org.apache.ibatis.annotations.*;

import java.util.Arrays;

public class BeanJudge {

    public static boolean isComponent(Class<?> clazz) {
        return clazz.isAnnotationPresent(Component.class) || clazz.isAnnotationPresent(Controller.class);
    }

    public static boolean isMapper(Class<?> clazz) {
        if (clazz.isAnnotationPresent(Mapper.class)) {
            return true;
        }

        var methods = clazz.getDeclaredMethods();
        for(var method : methods) {
            if (method.isAnnotationPresent(Insert.class) || method.isAnnotationPresent(Delete.class) ||
                    method.isAnnotationPresent(Select.class) || method.isAnnotationPresent(Update.class)) {
                return true;
            }
        }
        return false;
    }
}
