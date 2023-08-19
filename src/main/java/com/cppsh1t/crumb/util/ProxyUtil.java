package com.cppsh1t.crumb.util;



import com.cppsh1t.crumb.annotation.After;
import com.cppsh1t.crumb.annotation.AfterReturn;
import com.cppsh1t.crumb.annotation.Around;
import com.cppsh1t.crumb.annotation.Before;
import com.cppsh1t.crumb.exception.MethodRuleException;
import com.cppsh1t.crumb.proxy.AopBase;
import com.cppsh1t.crumb.proxy.JoinPoint;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ProxyUtil {

    public static Map<String, Method> getAopMethod(Object obj, AopBase aopBase) {
        var annoClass = switch (aopBase) {
            case BEFORE -> Before.class;
            case AFTER -> After.class;
            case AFTERRETURN -> AfterReturn.class;
            case AROUND -> Around.class;
        };

        var map = new HashMap<String, Method>();
        Arrays.stream(obj.getClass().getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(annoClass))
                .forEach(method -> {
                    String name = null;
                    Annotation anno = method.getAnnotation(annoClass);
                    if (Before.class.equals(annoClass)) {
                        name = ((Before) anno).value();
                    } else if (After.class.equals(annoClass)) {
                        name = ((After) anno).value();
                    } else if (AfterReturn.class.equals(annoClass)) {
                        name = ((AfterReturn) anno).value();
                    } else {
                        name = ((Around) anno).value();
                    }
                    map.put(name, method);
                });
        return map;
    }

    public static void invokeNormalMethod(Method method, Object invoker, Object[] args) {
        if (method.getParameterCount() == 0) {
            try {
                method.invoke(invoker, null);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new MethodRuleException("this aop Method can't invoke with noArgs");
            }
        } else if (method.getParameterCount() == 1 && method.getParameters()[0].getType() == Object[].class) {
            try {
                method.invoke(invoker, (Object) args);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new MethodRuleException("this aop Method can't invoke with noArgs，The parameter of type Object[] will be populated with the arguments of the original function");
            }
        } else {
            throw new MethodRuleException("this aop Method can't invoke，Please ensure method is noArgs or has a parameter of type Object[]");
        }
    }

    public static Object invokeAfterReturn(Method method, Object invoker, Object arg) {

        if (method.getParameterCount() != 1) {
            throw new MethodRuleException("AfterReturn requires a parameter of type Object and has a return type of Object");
        }

        if (method.getParameters()[0].getType() != Object.class
                && method.getReturnType() != Object.class) {
            throw new MethodRuleException("AfterReturn requires a parameter of type Object and has a return type of Object");
        }

        try {
            return method.invoke(invoker, (Object) arg);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new MethodRuleException("can't invoke this method");
        }
    }

    public static Object invokeAround(Method method, Object invoker, JoinPoint joinPoint) {
        try {
            return method.invoke(invoker, joinPoint);
        } catch (Exception e) {
            throw new MethodRuleException("Around requires a parameter of type JoinPoint and has a return type of Object");
        }
    }

}
