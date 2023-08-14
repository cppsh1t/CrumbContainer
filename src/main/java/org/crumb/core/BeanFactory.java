package org.crumb.core;

import ch.qos.logback.classic.Logger;
import lombok.extern.slf4j.Slf4j;
import org.crumb.annotation.Autowired;
import org.crumb.util.ClassConverter;
import org.crumb.util.ReflectUtil;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Collectors;

@Slf4j
public class BeanFactory {

    private final ObjectGetter objectGetter;
    private final Logger logger = (ch.qos.logback.classic.Logger) log;

    public BeanFactory(ObjectGetter objectGetter) {
        this.objectGetter = objectGetter;
        logger.setLevel(LoggerManager.currentLevel);
    }

    public Object getBean(Class<?> clazz) {
        var autowiredCon = ReflectUtil.getConstructorWithAnnotation(clazz, Autowired.class);
        if (autowiredCon != null) {
            var params = Arrays.stream(autowiredCon.getParameterTypes())
                    .map(ClassConverter::convertPrimitiveType)
                    .map(objectGetter::getObject).toArray();
            var instance = ReflectUtil.createInstance(autowiredCon, params);
            logger.debug("make the instance: " + instance + ", which use Autowired-Constructor: " + autowiredCon);
            return instance;
        } else {
            var instance = ReflectUtil.createInstance(clazz);
            logger.debug("make the instance: " + instance + ", which use noArgs-Constructor");
            return instance;
        }

    }

    //In this way, all the beans are singleton
    public Object getBean(Method method, Object invoker) {
        if (method.getParameterCount() == 0) {
            var instance = ReflectUtil.invokeMethod(method, invoker);
            logger.debug("make the instance: " + instance + ", which use method: " + method);
            return instance;
        } else {
            var params = Arrays.stream(method.getParameterTypes())
                    .map(objectGetter::getObject)
                    .collect(Collectors.toList());
            var instance = ReflectUtil.invokeMethod(method, invoker, params);
            logger.debug("make the instance: " + instance + ", which use method: " + method + ", params: " + params);
            return instance;
        }
    }

    public void injectBean(Object bean) {
        var fields = ReflectUtil.getFieldsWithAnnotation(bean.getClass(), Autowired.class);
        fields.forEach(field -> {
            var value = objectGetter.getObject(field.getType());
            ReflectUtil.setFieldValue(field, bean, value);
            logger.debug("set value: " + value + " on field: " + field.getName() + ", targetBean: " + bean);
        });
    }
}
