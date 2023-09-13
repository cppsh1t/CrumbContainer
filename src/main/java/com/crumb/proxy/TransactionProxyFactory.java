package com.crumb.proxy;

import com.crumb.annotation.Autowired;
import com.crumb.core.ObjectGetterByType;
import com.crumb.core.ProxyProvider;
import com.crumb.data.Transactional;
import com.crumb.definition.BeanDefinition;
import com.crumb.exception.MethodRuleException;
import com.crumb.util.ReflectUtil;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.session.SqlSessionFactory;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Collectors;

@Slf4j
public class TransactionProxyFactory implements ProxyFactory {


    protected final ByteBuddy buddy = new ByteBuddy();
    protected final ObjectGetterByType objectGetterByType;

    public TransactionProxyFactory(ObjectGetterByType objectGetterByType) {
        this.objectGetterByType = objectGetterByType;
    }

    @Override
    public Object makeProxy(Object origin, Object aopObj) {
        Class<?> clazz = origin.getClass();
        Class<?> proxyType;
        boolean isTran = Arrays.stream(clazz.getDeclaredMethods()).anyMatch(m -> m.isAnnotationPresent(Transactional.class));

        if (!isTran) {
            proxyType = buddy.subclass(clazz)
                    .implement(ProxyObject.class)
                    .method(ElementMatchers.any())
                    .intercept(MethodDelegation.to(new GeneralInterceptor(origin, aopObj)))
                    .make().load(getClass().getClassLoader()).getLoaded();
        } else {
            var sqlSessionFactory = (SqlSessionFactory) objectGetterByType.getObject(SqlSessionFactory.class);

            proxyType = buddy.subclass(clazz)
                    .implement(ProxyObject.class)
                    .method(ElementMatchers.any())
                    .intercept(MethodDelegation.to(new TransactionInterceptor(origin, aopObj, sqlSessionFactory)))
                    .make().load(getClass().getClassLoader()).getLoaded();
        }



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

    protected Object createProxyInstance(Class<?> clazz, Class<?>[] paramTypes) {
        if (paramTypes == null) {
            try {
                var instance = ReflectUtil.createInstance(clazz);
                log.debug("Create the proxyInstance: {}", instance);
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
        log.debug("Create the proxyInstance: {}", instance);
        return instance;
    }

}
