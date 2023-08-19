package com.cppsh1t.crumb.proxy;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class JoinPoint {

    private final Object[] args;
    private final Object target;
    private final Method method;

    public JoinPoint(Object target, Method method, Object[] args) {
        this.args = args;
        this.method = method;
        this.target = target;
    }

    public Object[] getArgs() {
        return args;
    }

    public Object getTarget() {
        return target;
    }

    public Object proceed(Object[] args) {
        try {
            return method.invoke(target, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("方法格式不正确");
        }
    }


}
