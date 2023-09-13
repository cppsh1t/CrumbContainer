package com.crumb.proxy;

import com.crumb.util.ProxyUtil;
import com.crumb.util.ReflectUtil;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.stream.Collectors;

public class GeneralInterceptor {
    private final Object origin;
    private final Object aopObj;

    private final Map<String, Method> beforeMethods;
    private final Map<String, Method> afterMethods;
    private final Map<String, Method> afterReturnMethods;
    private final Map<String, Method> aroundMethods;

    public GeneralInterceptor(Object origin, Object aopObj) {
        this.origin = origin;
        this.aopObj = aopObj;
        beforeMethods = ProxyUtil.getAopMethod(aopObj, AopBase.BEFORE);
        afterMethods = ProxyUtil.getAopMethod(aopObj, AopBase.AFTER);
        afterReturnMethods = ProxyUtil.getAopMethod(aopObj, AopBase.AFTERRETURN);
        aroundMethods = ProxyUtil.getAopMethod(aopObj, AopBase.AROUND);
    }


    @RuntimeType
    public Object intercept(@AllArguments Object[] args,
                            @Origin Method method) {
        Object result;

        if (method.getName().equals("getOrigin")) {
            return origin;
        }

        beforeMethods.keySet().stream()
                .filter(name -> name.equals(method.getName()))
                .map(beforeMethods::get)
                .forEach(m -> ProxyUtil.invokeNormalMethod(m, aopObj, args));

        var aroundMethod = aroundMethods.keySet().stream()
                .filter(name -> name.equals(method.getName()))
                .map(aroundMethods::get).findFirst().orElse(null);
        if (aroundMethod != null) {
            var point = new JoinPoint(origin, method, args);
            result = ProxyUtil.invokeAround(aroundMethod, aopObj, point);
        } else {
            result = ReflectUtil.invokeMethod(method, origin, args);
        }

        if (result != null) {
            var afm = afterReturnMethods.keySet().stream()
                    .filter(name -> name.equals(method.getName()))
                    .map(afterReturnMethods::get).collect(Collectors.toList());
            for(var m : afm) {
                result = ProxyUtil.invokeAfterReturn(m, aopObj, result);
            }
        }

        afterMethods.keySet().stream()
                .filter(name -> name.equals(method.getName()))
                .map(afterMethods::get)
                .forEach(m -> ProxyUtil.invokeNormalMethod(m, aopObj, args));
        return result;
    }
}
