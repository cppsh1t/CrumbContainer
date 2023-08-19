package com.cppsh1t.crumb.proxy;

import com.cppsh1t.crumb.util.ClassConverter;
import lombok.extern.slf4j.Slf4j;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import com.cppsh1t.crumb.annotation.Autowired;
import com.cppsh1t.crumb.core.ObjectGetter;
import com.cppsh1t.crumb.exception.MethodRuleException;
import com.cppsh1t.crumb.util.ProxyUtil;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Collectors;

@Slf4j
public class ProxyFactory {

    private final ObjectGetter objectGetter;
    private final Enhancer enhancer = new Enhancer();

    public ProxyFactory(ObjectGetter objectGetter) {
        this.objectGetter = objectGetter;
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
                    .map(objectGetter::getObject).toArray();
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
