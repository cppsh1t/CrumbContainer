package org.crumb.container;

import org.crumb.annotation.Autowired;
import org.crumb.util.ClassConverter;
import org.crumb.util.ReflectUtil;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Collectors;

public class BeanFactory {

    private final ObjectGetter objectGetter;

    public BeanFactory(ObjectGetter objectGetter) {
        this.objectGetter = objectGetter;
    }

    public Object getBean(Class<?> clazz) {
        var autowiredCon = ReflectUtil.getConstructorWithAnnotation(clazz, Autowired.class);
        if (autowiredCon != null) {
            var params = Arrays.stream(autowiredCon.getParameterTypes())
                    .map(ClassConverter::convertPrimitiveType)
                    .map(objectGetter::getObject).toArray();
            return ReflectUtil.createInstance(autowiredCon, params);
        } else {
            return ReflectUtil.createInstance(clazz);
        }

    }

    //In this way, all the beans are singleton
    public Object getBean(Method method, Object invoker) {
        if (method.getParameterCount() == 0) {
            return ReflectUtil.invokeMethod(method, invoker);
        } else {
            var params = Arrays.stream(method.getParameterTypes())
                    .map(objectGetter::getObject)
                    .collect(Collectors.toList());
            return ReflectUtil.invokeMethod(method, invoker, params);
        }
    }

    public void injectBean(Object bean) {
        var fields = ReflectUtil.getFieldsWithAnnotation(bean.getClass(), Autowired.class);
        fields.forEach(field -> {
            var value = objectGetter.getObject(field.getType());
            ReflectUtil.setFieldValue(field, bean, value);
        });
    }
}
