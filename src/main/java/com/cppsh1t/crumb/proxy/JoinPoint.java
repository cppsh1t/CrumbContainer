package com.cppsh1t.crumb.proxy;

import com.cppsh1t.crumb.exception.MethodRuleException;

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
            e.printStackTrace();
            throw new MethodRuleException("can't invoke the method");
        }
    }

    public Object proceed() {
        try {
            return method.invoke(target, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            throw new MethodRuleException("can't invoke the method");
        }
    }


}
