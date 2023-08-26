package com.crumb.proxy;

import com.crumb.util.ClassConverter;
import com.crumb.annotation.Autowired;
import com.crumb.util.ProxyUtil;
import lombok.extern.slf4j.Slf4j;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import com.crumb.core.ObjectGetterByType;
import com.crumb.exception.MethodRuleException;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Collectors;

@Slf4j
public class DefaultProxyFactory implements ProxyFactory {

    private final ObjectGetterByType objectGetterByType;
    private final Enhancer enhancer = new Enhancer();

    public DefaultProxyFactory(ObjectGetterByType objectGetterByType) {
        this.objectGetterByType = objectGetterByType;
    }

    public Object makeProxy(Object origin, Object aopObj) {
        var beforeMethods = ProxyUtil.getAopMethod(aopObj, AopBase.BEFORE);
        var afterMethods = ProxyUtil.getAopMethod(aopObj, AopBase.AFTER);
        var afterReturnMethods = ProxyUtil.getAopMethod(aopObj, AopBase.AFTERRETURN);
        var aroundMethods = ProxyUtil.getAopMethod(aopObj, AopBase.AROUND);
        var clazz = origin.getClass();

        enhancer.setSuperclass(clazz);
        enhancer.setInterfaces(new Class[]{ProxyObject.class});
        enhancer.setCallback(new MethodInterceptor() {

            @Override
            public Object intercept(Object no, Method method, Object[] args, MethodProxy no2) throws Throwable {
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
                    result = method.invoke(origin, args);
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
        });

        return createProxyInstance(clazz);
    }

    private Object createProxyInstance(Class<?> clazz) {
        var autoCon = Arrays.stream(clazz.getDeclaredConstructors())
                .filter(con -> con.isAnnotationPresent(Autowired.class))
                .findFirst().orElse(null);

        if (autoCon != null) {
            var paramTypes = Arrays.stream(autoCon.getParameterTypes())
                    .map(ClassConverter::convertPrimitiveType).toArray(Class<?>[]::new);
            var params = Arrays.stream(paramTypes)
                    .map(objectGetterByType::getObject).toArray();
            var instance = enhancer.create(paramTypes, params);
            log.debug("create the proxyInstance: {}", instance);
            return instance;
        }

        try {
            var instance = enhancer.create();
            log.debug("create the proxyInstance: {}", instance);
            return instance;
        } catch (RuntimeException e) {
            throw new MethodRuleException("Missing constructors available for proxy use");
        }

    }


}
