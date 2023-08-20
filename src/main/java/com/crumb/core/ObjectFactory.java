package com.crumb.core;

import com.crumb.annotation.Autowired;
import com.crumb.util.ClassConverter;
import com.crumb.util.ReflectUtil;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Collectors;

@Slf4j
public class ObjectFactory implements BeanFactory {

    private final ObjectGetter objectGetter;

    public ObjectFactory(ObjectGetter objectGetter) {
        this.objectGetter = objectGetter;
    }

    public <T> T getBean(Class<T> clazz) {
        var autowiredCon = ReflectUtil.getConstructorWithAnnotation(clazz, Autowired.class);
        if (autowiredCon != null) {
            var params = Arrays.stream(autowiredCon.getParameterTypes())
                    .map(ClassConverter::convertPrimitiveType)
                    .map(objectGetter::getObject).toArray();
            var instance = ReflectUtil.createInstance(autowiredCon, params);
            log.debug("make the instance: {}, which use Autowired-Constructor: {}", instance, autowiredCon);
            return (T) instance;
        } else {
            var instance = ReflectUtil.createInstance(clazz);
            log.debug("make the instance: {}, which use noArgs-Constructor", instance);
            return instance;
        }

    }

    //In this way, all the beans are singleton
    public Object getBean(Method method, Object invoker) {
        if (method.getParameterCount() == 0) {
            var instance = ReflectUtil.invokeMethod(method, invoker);
            log.debug("make the instance: {}, which use method: {}", instance, method);
            return instance;
        } else if (method.getParameterCount() == 1) {
            var param = Arrays.stream(method.getParameterTypes())
                    .map(objectGetter::getObject).findFirst().orElse(null);
            var instance = ReflectUtil.invokeMethod(method, invoker, param);
            log.debug("make the instance: {}, which use method: {}, params: {}", instance, method, param);
            return instance;
        } else {
            var params = Arrays.stream(method.getParameterTypes())
                    .map(objectGetter::getObject)
                    .collect(Collectors.toList());
            var instance = ReflectUtil.invokeMethod(method, invoker, params);
            log.debug("make the instance: {}, which use method: {}, params: {}", instance, method, params);
            return instance;
        }
    }

    public void injectBean(Object bean) {
        var fields = ReflectUtil.getFieldsWithAnnotation(bean.getClass(), Autowired.class);
        fields.forEach(field -> {
            var value = objectGetter.getObject(field.getType());
            ReflectUtil.setFieldValue(field, bean, value);
            log.debug("set value: {} on field: {}, targetBean: {}", value, field.getName(), bean);
        });
    }
}
