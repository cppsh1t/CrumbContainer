package com.crumb.proxy;

import com.crumb.core.ObjectGetterByType;
import com.crumb.data.Transactional;
import com.crumb.util.ProxyUtil;
import com.crumb.util.ReflectUtil;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.session.SqlSessionFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class TransactionInterceptor {
    private final SqlSessionFactory sqlSessionFactory;

    private final Object origin;
    private final Object aopObj;

    private final boolean hasAop;
    private final List<Field> mapperFields;
    private final List<Class<?>> mapperTypes;

    private Map<String, Method> beforeMethods;
    private Map<String, Method> afterMethods;
    private Map<String, Method> afterReturnMethods;
    private Map<String, Method> aroundMethods;


    public TransactionInterceptor(Object origin, Object aopObj, SqlSessionFactory sqlSessionFactory) {
        this.origin = origin;
        this.aopObj = aopObj;
        this.sqlSessionFactory = sqlSessionFactory;

        if (aopObj != null) {
            hasAop = true;
            beforeMethods = ProxyUtil.getAopMethod(aopObj, AopBase.BEFORE);
            afterMethods = ProxyUtil.getAopMethod(aopObj, AopBase.AFTER);
            afterReturnMethods = ProxyUtil.getAopMethod(aopObj, AopBase.AFTERRETURN);
            aroundMethods = ProxyUtil.getAopMethod(aopObj, AopBase.AROUND);
        } else {
            hasAop = false;
        }

        mapperFields = Arrays.stream(origin.getClass().getDeclaredFields())
                .filter(f -> f.getType().isAnnotationPresent(Mapper.class)).collect(Collectors.toList());
        mapperTypes = mapperFields.stream()
                .map(Field::getType).collect(Collectors.toList());
    }


    @RuntimeType
    public Object intercept(@AllArguments Object[] args,
                            @Origin Method method) {
        Object result;
        boolean isTran = method.isAnnotationPresent(Transactional.class);

        if (method.getName().equals("getOrigin")) {
            return origin;
        }

        if (hasAop) {
            beforeMethods.keySet().stream()
                    .filter(name -> name.equals(method.getName()))
                    .map(beforeMethods::get)
                    .forEach(m -> ProxyUtil.invokeNormalMethod(m, aopObj, args));
        }

        if (hasAop) {
            var aroundMethod = aroundMethods.keySet().stream()
                    .filter(name -> name.equals(method.getName()))
                    .map(aroundMethods::get).findFirst().orElse(null);
            if (aroundMethod != null) {
                var point = new JoinPoint(origin, method, args);
                result = ProxyUtil.invokeAround(aroundMethod, aopObj, point);
            } else {
                if (isTran) {
                    result = doTransactionProcess(method, origin, args);
                } else {
                    result = ReflectUtil.invokeMethod(method, origin, args);
                }

            }
        } else {
            if (isTran) {
                result = doTransactionProcess(method, origin, args);
            } else {
                result = ReflectUtil.invokeMethod(method, origin, args);
            }

        }


        if (hasAop) {
            if (result != null) {
                var afm = afterReturnMethods.keySet().stream()
                        .filter(name -> name.equals(method.getName()))
                        .map(afterReturnMethods::get).collect(Collectors.toList());
                for (var m : afm) {
                    result = ProxyUtil.invokeAfterReturn(m, aopObj, result);
                }
            }

            afterMethods.keySet().stream()
                    .filter(name -> name.equals(method.getName()))
                    .map(afterMethods::get)
                    .forEach(m -> ProxyUtil.invokeNormalMethod(m, aopObj, args));
        }

        return result;
    }


    public Object doTransactionProcess(Method method, Object invoker, Object... args) {

        var oldMapperValues = mapperFields.stream()
                .map(f -> ReflectUtil.getFieldValue(f, invoker)).collect(Collectors.toList());

        var tranSession = sqlSessionFactory.openSession(false);
        var newMapperValues = mapperTypes.stream().map(tranSession::getMapper).collect(Collectors.toList());

        for (int i = 0; i < mapperFields.size(); i++) {
            var field = mapperFields.get(i);
            var newValue = newMapperValues.get(i);
            ReflectUtil.setFieldValue(field, invoker, newValue);
        }
        Object result = null;
        try {
            log.debug("Performing transactional operation: {}", method);
            result = method.invoke(invoker, args);
            tranSession.commit();
        } catch (Exception exception) {
            tranSession.rollback();
            log.debug("An exception: {} occurred, and it has been automatically rolled back", exception.getCause().getClass());
        }

        for (int i = 0; i < mapperFields.size(); i++) {
            var field = mapperFields.get(i);
            var oldValue = oldMapperValues.get(i);
            ReflectUtil.setFieldValue(field, invoker, oldValue);
        }

        tranSession.close();

        return result;
    }
}
