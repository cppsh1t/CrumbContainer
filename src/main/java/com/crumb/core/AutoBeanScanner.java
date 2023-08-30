package com.crumb.core;

import com.crumb.builder.BeanDefinitionBuilder;
import com.crumb.definition.BeanDefinition;
import com.crumb.definition.BeanJudge;
import com.crumb.util.ReflectUtil;
import io.github.classgraph.ClassGraph;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
public class AutoBeanScanner implements BeanScanner{

    private final BeanScanner defaultScanner = new DefaultBeanScanner();

    @Override
    public List<BeanDefinition> getBeanDefinition(Class<?> clazz) {
        String packName = ReflectUtil.getTopLevelPackage(clazz.getPackageName());
        var list = new ArrayList<BeanDefinition>();
        ClassGraph classGraph = new ClassGraph();
        classGraph.enableAnnotationInfo();
        classGraph.acceptPackages(packName);
        var componentTypes = BeanJudge.getComponentTypeAnno();
        componentTypes.forEach(c -> {
            var classInfos = classGraph.scan().getClassesWithAnnotation(c);
            var classes = classInfos.loadClasses();
            classes.forEach(comType -> {
                var def = BeanDefinitionBuilder.getComponentDef(comType);
                list.add(def);
                log.debug("get beanDefinition: {}", def);
            });
        });
        return list;
    }

    @Override
    public Map<Class<?>, BeanDefinition> getFactoryBeanDefinition(Set<BeanDefinition> definitions) {
        return defaultScanner.getFactoryBeanDefinition(definitions);
    }

    @Override
    public List<Method> getBeanMethod(Class<?> clazz) {
        return defaultScanner.getBeanMethod(clazz);
    }

    @Override
    public Map<Class<?>, Method> getFactoryBeanMethods(Set<Method> methods) {
        return defaultScanner.getFactoryBeanMethods(methods);
    }

    @Override
    public Map<Class<?>, BeanDefinition> getAopBeanDefinition(Set<BeanDefinition> definitions) {
        return defaultScanner.getAopBeanDefinition(definitions);
    }
}
