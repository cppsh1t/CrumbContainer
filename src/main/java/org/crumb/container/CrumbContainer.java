package org.crumb.container;

import org.crumb.annotation.Autowired;
import org.crumb.annotation.Lazy;
import org.crumb.annotation.ScopeType;
import org.crumb.definition.BeanDefinition;
import org.crumb.exception.BeanNotFoundException;
import org.crumb.util.ClassConverter;
import org.crumb.util.ReflectUtil;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class CrumbContainer {

    private boolean canOverride = false;

    private final BeanScanner scanner = new BeanScanner();
    private BeanFactory beanFactory;

    private final Class<?> configClass;
    private Object configObj;

    private final Set<BeanDefinition> beanDefSet = new HashSet<>();
    private final Set<Method> beanMethods = ConcurrentHashMap.newKeySet();
    private final Set<BeanDefinition> remainBeanDefSet = ConcurrentHashMap.newKeySet();

    private final Map<BeanDefinition, Object> singletonObjects = new ConcurrentHashMap<>();
    private final Map<BeanDefinition, Object> earlySingletonObjects = new ConcurrentHashMap<>();
    private final Map<BeanDefinition, Object> prototypeCache = new ConcurrentHashMap<>();
    private final Map<Class<?>, BeanDefinition> factoryBeanMap = new ConcurrentHashMap<>();


    public CrumbContainer(Class<?> configClass) {
        this.configClass = configClass;
        initContext();
    }

    private void initContext() {
        initChildrenModules();
        processConfig();
        createComponents();
    }

    private void processConfig() {
        var scanPaths = scanner.getComponentScanPath(configClass);
        var classFiles = scanPaths.stream()
                .map(scanner::getComponentFile)
                .flatMap(List::stream)
                .collect(Collectors.toList());
        beanDefSet.addAll(scanner.getBeanDefinition(classFiles));
        factoryBeanMap.putAll(scanner.getFactoryBeanDefinition(beanDefSet));
        remainBeanDefSet.addAll(beanDefSet.stream().filter(def -> def.scope == ScopeType.SINGLETON
                && !def.clazz.isAnnotationPresent(Lazy.class)).collect(Collectors.toSet()));
        beanMethods.addAll(scanner.getBeanMethod(configClass));
        configObj = ReflectUtil.createInstance(configClass);
        injectConfigObj();
    }

    private void initChildrenModules() {
        beanFactory = new BeanFactory(clazz -> getBean(clazz, true));
    }

    private void createComponents() {
        for(var def : remainBeanDefSet) {
            createBean(def);
        }
        for(var method : beanMethods) {
            createBean(method);
        }
    }

    private Object getBean(Class<?> clazz, boolean useBeanMethod) {
        var definition = getBeanDefinition(clazz);
        if (definition != null) {
            return createBean(definition);
        } else if (useBeanMethod) {
            return createBean(clazz);
        } else {
            return Optional.ofNullable(createBeanFromFactoryBean(clazz))
                    .orElseThrow(() -> new BeanNotFoundException(clazz));
        }
    }

    private Object createBean(BeanDefinition definition) {
        if (definition.scope == ScopeType.PROTOTYPE) {
            var instance = prototypeCache.getOrDefault(definition, beanFactory.getBean(definition.clazz));
            injectBean(instance, definition, true);
            return instance;
        }
        // definition.scope == ScopeType.SINGLETON
        var instance = singletonObjects.getOrDefault(definition, earlySingletonObjects.getOrDefault(definition, null));
        if (instance == null) {
            remainBeanDefSet.remove(definition);
            instance = beanFactory.getBean(definition.clazz);
            injectBean(instance, definition, false);
            registerBean(definition, instance);
        }

        return instance;
    }

    private Object createBean(Class<?> targetType) {
        Method method = getBeanMethod(targetType);
        if (method == null) throw new BeanNotFoundException(targetType);
        return createBean(method);
    }

    private Object createBean(Method method) {
        var instance = beanFactory.getBean(method, configObj);
        beanMethods.remove(method);
        registerBean(new BeanDefinition(instance.getClass(), ScopeType.SINGLETON), instance);
        return instance;
    }

    private Object createBeanFromFactoryBean(Class<?> clazz) {
        var def = factoryBeanMap.get(clazz);
        if (def == null) return null;
        var factoryBean = (FactoryBean<?>) createBean(def);
        boolean isSingleton = factoryBean.isSingleton();
        var bean = factoryBean.getObject();
        if (isSingleton) {
            factoryBeanMap.remove(clazz);
            var beanDef = new BeanDefinition(clazz, ScopeType.SINGLETON);
            registerBean(beanDef, bean);
        }
        return bean;
    }

    private void injectBean(Object bean, BeanDefinition definition, boolean isPrototype) {
        if (!ReflectUtil.hasAnnotationOnField(definition.clazz, Autowired.class)) return;
        var targetCache = isPrototype ? prototypeCache : earlySingletonObjects;

        if (!targetCache.containsKey(definition)) {
            targetCache.put(definition, bean);
            beanFactory.injectBean(bean);
            targetCache.remove(definition);
        }
    }

    private void injectConfigObj() {
        if (!ReflectUtil.hasAnnotationOnField(configClass, Autowired.class)) return;
        beanFactory.injectBean(configObj);
    }

    public <T> T getBean(Class<T> clazz) {
        return (T) getBean(clazz, false);
    }

    public boolean registerBean(BeanDefinition definition, Object object) {
        if (!singletonObjects.containsKey(definition) || canOverride)  {
            singletonObjects.put(definition, object);
            beanDefSet.add(definition);
            return true;
        } else return false;
    }

    public BeanDefinition getBeanDefinition(Class<?> clazz) {
        return beanDefSet.stream().filter(def -> def.clazz == clazz).findFirst().orElse(null);
    }

    private Method getBeanMethod(Class<?> returnType) {
        return beanMethods.stream()
                .filter(method -> ClassConverter.convertPrimitiveType(method.getReturnType()) == returnType)
                .findFirst()
                .orElse(null);
    }

    public void logBeanDefs() {
        beanDefSet.forEach(System.out::println);
    }

    public void setOverride(boolean canOverride) {
        this.canOverride = canOverride;
    }
}
