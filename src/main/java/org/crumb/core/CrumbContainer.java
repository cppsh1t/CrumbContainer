package org.crumb.core;

import ch.qos.logback.classic.Logger;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
public class CrumbContainer {

    private static final String GREEN = "\u001B[32m";
    private static final String RESET = "\u001B[0m";

    private boolean canOverride = false;

    private BeanScanner scanner;
    private BeanFactory beanFactory;
    private final Logger logger = (ch.qos.logback.classic.Logger) log;
    private final PropFactory propFactory = new PropFactory();

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
        var level = (String) propFactory.getPropValueNoThrow("crumb.logger.level");
        if (level != null) LoggerManager.setLoggerLevel(level);
        logger.setLevel(LoggerManager.currentLevel);
        this.configClass = configClass;
        initContext();
    }

    private void initContext() {
        initChildrenModules();
        processConfig();
        createComponents();
    }

    private void processConfig() {
        logger.debug(GREEN + "start processing configuration" + RESET);
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
        logger.debug(GREEN + "end processing configuration" + RESET);
    }

    private void initChildrenModules() {
        logger.debug(GREEN + "start initializing childrenModules" + RESET);
        scanner = new BeanScanner();
        beanFactory = new BeanFactory(clazz -> getBean(clazz, true));
        logger.debug(GREEN + "end initializing childrenModules" + RESET);
    }

    private void createComponents() {
        logger.debug(GREEN + "start creating components" + RESET);
        for(var def : remainBeanDefSet) {
            var component = createBean(def);
            logger.debug("proactively created the component: {}", component);
        }
        for(var method : beanMethods) {
            var component = createBean(method);
            logger.debug("proactively created the component: {}", component);
        }
        logger.debug(GREEN + "end creating components" + RESET);
    }

    private Object getBean(Class<?> clazz, boolean useBeanMethod) {
        var finalClazz = ClassConverter.convertPrimitiveType(clazz);
        logger.debug("want to find Bean which class: {}", finalClazz.getName());
        var definition = getBeanDefinition(finalClazz);
        if (definition != null) {
            return createBean(definition);
        } else if (useBeanMethod) {
            return createBean(finalClazz);
        } else {
            return Optional.ofNullable(createBeanFromFactoryBean(finalClazz))
                    .orElseThrow(() -> new BeanNotFoundException(finalClazz));
        }
    }

    private Object createBean(BeanDefinition definition) {
        logger.debug("want to get Bean: {}", definition);
        if (definition.scope == ScopeType.PROTOTYPE) {
            var instance = prototypeCache.getOrDefault(definition, beanFactory.getBean(definition.clazz));
            propFactory.setPropsValue(instance);
            injectBean(instance, definition, true);
            return instance;
        }
        // definition.scope == ScopeType.SINGLETON
        var instance = singletonObjects.getOrDefault(definition, earlySingletonObjects.getOrDefault(definition, null));
        if (instance == null) {
            remainBeanDefSet.remove(definition);
            instance = beanFactory.getBean(definition.clazz);
            propFactory.setPropsValue(instance);
            injectBean(instance, definition, false);
            registerBean(definition, instance);
        }

        return instance;
    }

    private Object createBean(Class<?> targetType) {
        logger.debug("want to get Bean which class: {}", targetType.getName());
        Method method = getBeanMethod(targetType);
        if (method == null) throw new BeanNotFoundException(targetType);
        return createBean(method);
    }

    private Object createBean(Method method) {
        logger.debug("want to get Bean which use method: {}", method);
        var instance = beanFactory.getBean(method, configObj);
        beanMethods.remove(method);
        registerBean(new BeanDefinition(instance.getClass(), ScopeType.SINGLETON), instance);
        return instance;
    }

    private Object createBeanFromFactoryBean(Class<?> clazz) {
        logger.debug("want to get Bean from FactoryBean, which class: {}", clazz);
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
        logger.debug("want to inject Bean: {}, which definition: {}", bean, definition);
        var targetCache = isPrototype ? prototypeCache : earlySingletonObjects;

        if (!targetCache.containsKey(definition)) {
            logger.debug("put {} into cache", bean);
            targetCache.put(definition, bean);
            beanFactory.injectBean(bean);
            targetCache.remove(definition);
            logger.debug("remove {} from cache", bean);
        }
    }

    private void injectConfigObj() {
        if (!ReflectUtil.hasAnnotationOnField(configClass, Autowired.class)) return;
        propFactory.setPropsValue(configObj);
        beanFactory.injectBean(configObj);
    }

    public <T> T getBean(Class<T> clazz) {
        return (T) getBean(clazz, false);
    }

    public boolean registerBean(BeanDefinition definition, Object object) {
        if (!singletonObjects.containsKey(definition) || canOverride)  {
            singletonObjects.put(definition, object);
            beanDefSet.add(definition);
            logger.debug("register Bean: {}, which definition: {}", object, definition);
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
