package com.crumb.proxy;

import com.crumb.annotation.Autowired;
import com.crumb.core.ObjectGetterByType;
import com.crumb.exception.MethodRuleException;
import com.crumb.util.ReflectUtil;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;


import java.util.Arrays;

@Slf4j
public class DefaultProxyFactory implements ProxyFactory {

    private final ByteBuddy buddy = new ByteBuddy();
    private final ObjectGetterByType objectGetterByType;

    public DefaultProxyFactory(ObjectGetterByType objectGetterByType) {
        this.objectGetterByType = objectGetterByType;
    }

    @Override
    public Object makeProxy(Object origin, Object aopObj) {
        Class<?> clazz = origin.getClass();
        Class<?> proxyType = buddy.subclass(clazz)
                .implement(ProxyObject.class)
                .method(ElementMatchers.any())
                .intercept(MethodDelegation.to(new GeneralInterceptor(origin, aopObj)))
                .make().load(getClass().getClassLoader()).getLoaded();

        var autoCon = Arrays.stream(clazz.getDeclaredConstructors())
                .filter(con -> con.isAnnotationPresent(Autowired.class))
                .findFirst().orElse(null);
        if (autoCon != null) {
            var paramTypes = autoCon.getParameterTypes();
            return createProxyInstance(proxyType, paramTypes);
        } else {
            return createProxyInstance(proxyType, null);
        }
    }

    private Object createProxyInstance(Class<?> clazz, Class<?>[] paramTypes) {
        if (paramTypes == null) {
            try {
                var instance = ReflectUtil.createInstance(clazz);
                log.debug("create the proxyInstance: {}", instance);
                return instance;
            } catch (RuntimeException e) {
                throw new MethodRuleException("Missing constructors available for proxy use");
            }
        }

        var autoCon = Arrays.stream(clazz.getDeclaredConstructors())
                .filter(c -> Arrays.equals(c.getParameterTypes(), paramTypes)).findFirst().orElseThrow();
        var params = Arrays.stream(paramTypes)
                .map(objectGetterByType::getObject).toArray();
        var instance = ReflectUtil.createInstance(autoCon, params);
        log.debug("create the proxyInstance: {}", instance);
        return instance;


    }


}
