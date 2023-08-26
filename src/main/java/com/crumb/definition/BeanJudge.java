package com.crumb.definition;

import com.crumb.annotation.Component;
import com.crumb.web.Controller;
import com.crumb.web.Service;
import org.apache.ibatis.annotations.*;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

public class BeanJudge {

    public static Set<Class<? extends Annotation>> getComponentTypeAnno() {
        var set = new HashSet<Class<? extends Annotation>>();
        set.add(Component.class);set.add(Controller.class);set.add(Service.class);
        return set;
    }

    public static boolean isComponent(Class<?> clazz) {
        return clazz.isAnnotationPresent(Component.class)
                || clazz.isAnnotationPresent(Controller.class)
                || clazz.isAnnotationPresent(Service.class);
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
