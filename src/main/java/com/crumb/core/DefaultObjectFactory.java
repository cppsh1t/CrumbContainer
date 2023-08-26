package com.crumb.core;

import com.crumb.annotation.Autowired;
import com.crumb.util.ClassConverter;
import com.crumb.util.ReflectUtil;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Collectors;

@Slf4j
public class DefaultObjectFactory implements ObjectFactory {

    private final ObjectGetterByType objectGetterByType;
    private final ObjectGetterByName objectGetterByName;

    public DefaultObjectFactory(ObjectGetterByType objectGetterByType, ObjectGetterByName objectGetterByName) {
        this.objectGetterByType = objectGetterByType;
        this.objectGetterByName = objectGetterByName;
    }

    public <T> T getBean(Class<T> clazz) {
        var autowiredCon = ReflectUtil.getConstructorWithAnnotation(clazz, Autowired.class);
        if (autowiredCon != null) {
            var params = Arrays.stream(autowiredCon.getParameterTypes())
                    .map(ClassConverter::convertPrimitiveType)
                    .map(objectGetterByType::getObject).toArray();
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
                    .map(objectGetterByType::getObject).findFirst().orElse(null);
            var instance = ReflectUtil.invokeMethod(method, invoker, param);
            log.debug("make the instance: {}, which use method: {}, params: {}", instance, method, param);
            return instance;
        } else {
            var params = Arrays.stream(method.getParameterTypes())
                    .map(objectGetterByType::getObject)
                    .collect(Collectors.toList());
            var instance = ReflectUtil.invokeMethod(method, invoker, params);
            log.debug("make the instance: {}, which use method: {}, params: {}", instance, method, params);
            return instance;
        }
    }

    public void injectBean(Object bean) {
        var fields = ReflectUtil.getInjectableFields(bean.getClass());
        fields.forEach(field -> {
            Object value = null;
            if (field.isAnnotationPresent(Autowired.class)) {
                value = objectGetterByType.getObject(field.getType());
            } else {
                value = objectGetterByName.getObject(field.getName());
            }
            ReflectUtil.setFieldValue(field, bean, value);
            log.debug("set value: {} on field: {}, targetBean: {}", value, field.getName(), bean);
        });
    }
}
