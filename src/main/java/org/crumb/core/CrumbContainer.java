package org.crumb.core;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import lombok.extern.slf4j.Slf4j;
import org.crumb.annotation.Autowired;
import org.crumb.annotation.Lazy;
import org.crumb.annotation.ScopeType;
import org.crumb.definition.BeanDefinition;
import org.crumb.exception.BeanNotFoundException;
import org.crumb.util.ClassConverter;
import org.crumb.util.ReflectUtil;
import org.slf4j.LoggerFactory;

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
    private final PropFactory propFactory = new PropFactory();

    private final Class<?> configClass;
    private Object configObj;

    private final Set<BeanDefinition> beanDefSet = new HashSet<>();
    private final Set<Method> beanMethods = ConcurrentHashMap.newKeySet();
    private final Set<BeanDefinition> remainBeanDefSet = ConcurrentHashMap.newKeySet();
    private final Set<Method> remainBeanMethods = ConcurrentHashMap.newKeySet();

    private final Map<BeanDefinition, Object> singletonObjects = new ConcurrentHashMap<>();
    private final Map<BeanDefinition, Object> earlySingletonObjects = new ConcurrentHashMap<>();
    private final Map<BeanDefinition, Object> prototypeCache = new ConcurrentHashMap<>();
    private final Map<Class<?>, BeanDefinition> factoryBeanMap = new ConcurrentHashMap<>();

    public CrumbContainer(Class<?> configClass) {
        propFactory.logBanner();
        this.configClass = configClass;
        initContext();
    }

    private void initContext() {
        initChildrenModules();
        processConfig();
        createComponents();
        log.info("Successfully started crumbContainer");
    }

    private void processConfig() {
        log.debug(GREEN + "start processing configuration" + RESET);
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
        remainBeanMethods.addAll(beanMethods.stream()
                .filter(method -> !method.isAnnotationPresent(Lazy.class)).collect(Collectors.toSet()));

        configObj = ReflectUtil.createInstance(configClass);
        injectConfigObj();
        log.debug(GREEN + "end processing configuration" + RESET);
    }

    private void initChildrenModules() {
        log.debug(GREEN + "start initializing childrenModules" + RESET);
        scanner = new BeanScanner();
        beanFactory = new BeanFactory(clazz -> getBeanInside(clazz));
        log.debug(GREEN + "end initializing childrenModules" + RESET);
    }

    private void createComponents() {
        log.debug(GREEN + "start creating components" + RESET);
        for(var def : remainBeanDefSet) {
            var component = createBean(def);
            log.debug("proactively created the component: {}", component);
        }
        for(var method : remainBeanMethods) {
            var component = createBean(method);
            log.debug("proactively created the component: {}", component);
        }
        log.debug(GREEN + "end creating components" + RESET);
    }

    private Object getBeanInside(Class<?> clazz) {
        var finalClazz = ClassConverter.convertPrimitiveType(clazz);
        log.debug("want to find Bean which class: {}", finalClazz.getName());
        var definition = getBeanDefinition(finalClazz);
        if (definition != null) {
            return createBean(definition);
        } else {
             var bean = createBean(finalClazz);
             if (bean == null)
             {
                 bean = Optional.ofNullable(createBeanFromFactoryBean(finalClazz))
                         .orElseThrow(() -> new BeanNotFoundException(finalClazz));
             }
             return bean;
        }
    }

    private Object createBean(BeanDefinition definition) {
        log.debug("want to get Bean: {}", definition);
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
        log.debug("want to get Bean which class: {}", targetType.getName());
        Method method = getBeanMethod(targetType);
        if (method == null) return null;
        return createBean(method);
    }

    private Object createBean(Method method) {
        log.debug("want to get Bean which use method: {}", method);
        var instance = beanFactory.getBean(method, configObj);
        remainBeanMethods.remove(method);
        registerBean(new BeanDefinition(instance.getClass(), ScopeType.SINGLETON), instance);
        return instance;
    }

    private Object createBeanFromFactoryBean(Class<?> clazz) {
        log.debug("want to get Bean from FactoryBean, which class: {}", clazz);
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
        log.debug("want to inject Bean: {}, which definition: {}", bean, definition);
        var targetCache = isPrototype ? prototypeCache : earlySingletonObjects;

        if (!targetCache.containsKey(definition)) {
            log.debug("put {} into cache", bean);
            targetCache.put(definition, bean);
            beanFactory.injectBean(bean);
            targetCache.remove(definition);
            log.debug("remove {} from cache", bean);
        }
    }

    private void injectConfigObj() {
        if (!ReflectUtil.hasAnnotationOnField(configClass, Autowired.class)) return;
        propFactory.setPropsValue(configObj);
        beanFactory.injectBean(configObj);
    }

    public <T> T getBean(Class<T> clazz) {
        return (T) getBeanInside(clazz);
    }

    public boolean registerBean(BeanDefinition definition, Object object) {
        if (!singletonObjects.containsKey(definition) || canOverride)  {
            singletonObjects.put(definition, object);
            beanDefSet.add(definition);
            log.debug("register Bean: {}, which definition: {}", object, definition);
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

    public static void setLoggerLevel(Level level) {
        Logger logger = (Logger) LoggerFactory.getLogger("org.crumb.core");
        logger.setLevel(level);
    }

    public void close() {
        singletonObjects.clear();
        beanDefSet.clear();
        remainBeanDefSet.clear();
        beanMethods.clear();
        remainBeanMethods.clear();
        factoryBeanMap.clear();
    }
}
