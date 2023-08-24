package com.crumb.core;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.crumb.beanProcess.BeanPostProcessor;
import com.crumb.builder.BeanDefinitionBuilder;
import com.crumb.data.MapperScanner;
import com.crumb.definition.BeanDefinition;
import com.crumb.definition.BeanJudge;
import com.crumb.exception.BeanNotFoundException;
import com.crumb.proxy.ProxyFactory;
import com.crumb.util.ClassConverter;
import com.crumb.util.ReflectUtil;
import com.crumb.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import com.crumb.annotation.Autowired;
import com.crumb.annotation.EnableAspectProxy;
import com.crumb.annotation.Lazy;
import com.crumb.definition.ScopeType;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.crumb.misc.Color.*;

@Slf4j
public class CrumbContainer implements BeanFactory {

    private final List<String> mapperPaths = new ArrayList<>();

    private boolean canOverride = false;
    private boolean enableProxy = false;
    private boolean hasAddMappers = false;

    private final BeanScanner scanner = new BeanScanner();
    private final ObjectFactory objectFactory = new ObjectFactory(this::getBeanInside);
    private final ValuesFactory valuesFactory = new ValuesFactory();
    private final ProxyFactory proxyFactory = new ProxyFactory(this::getBeanInside);

    private final Class<?> configClass;
    private Object configObj;

    private final Set<BeanDefinition> beanDefSet = new HashSet<>();
    private final Set<Method> beanMethods = ConcurrentHashMap.newKeySet();
    private final Set<BeanDefinition> remainBeanDefSet = ConcurrentHashMap.newKeySet();
    private final Set<Method> remainBeanMethods = ConcurrentHashMap.newKeySet();
    private final Map<Class<?>, BeanDefinition> aopDefMap = new ConcurrentHashMap<>();

    private final Map<BeanDefinition, Object> singletonObjects = new ConcurrentHashMap<>();
    private final Map<BeanDefinition, Object> earlySingletonObjects = new ConcurrentHashMap<>();
    private final Map<BeanDefinition, Object> prototypeCache = new ConcurrentHashMap<>();

    private final Map<Class<?>, BeanDefinition> factoryBeanMap = new ConcurrentHashMap<>();
    private final Map<Class<?>, Method> factoryMethodBeanMap = new ConcurrentHashMap<>();

    private final Set<BeanDefinition> processorDefs = new HashSet<>();
    private final Set<BeanPostProcessor> postProcessors = new HashSet<>();

    private final BeanLifeCycle lifeCycle = new BeanLifeCycle(valuesFactory::setPropsValue, this::injectBean,
            this::proxyBean, this.postProcessors);

    public CrumbContainer(Class<?> configClass) {
        valuesFactory.logBanner();
        this.configClass = configClass;
        if (configClass.isAnnotationPresent(EnableAspectProxy.class)) {
            enableProxy = true;
            log.info(YELLOW + "Enable AspectProxy need VM parameter: --add-opens java.base/java.lang=ALL-UNNAMED" + RESET);
        }
        initContext();
    }

    private void initContext() {
        processConfig();
        createComponents();
        log.info(GREEN + "Successfully started crumbContainer" + RESET);
    }

    private void processConfig() {
        log.debug(BLUE + "start processing configuration" + RESET);
        mapperPaths.addAll(MapperScanner.getMapperPaths(configClass));
        var scanPaths = scanner.getComponentScanPath(configClass);
        var classFiles = scanner.getComponentFiles(scanPaths);

        beanDefSet.addAll(scanner.getBeanDefinition(classFiles));
        factoryBeanMap.putAll(scanner.getFactoryBeanDefinition(beanDefSet));
        remainBeanDefSet.addAll(beanDefSet.stream().filter(def -> def.scope == ScopeType.SINGLETON
                && !def.clazz.isAnnotationPresent(Lazy.class)).collect(Collectors.toSet()));

        beanMethods.addAll(scanner.getBeanMethod(configClass));
        factoryMethodBeanMap.putAll(scanner.getFactoryBeanMethods(beanMethods));
        remainBeanMethods.addAll(beanMethods.stream()
                .filter(method -> !method.isAnnotationPresent(Lazy.class)).collect(Collectors.toSet()));

        processorDefs.addAll(beanDefSet.stream()
                .filter(def -> BeanPostProcessor.class.isAssignableFrom(def.clazz))
                .collect(Collectors.toSet()));

        if (enableProxy) {
            aopDefMap.putAll(scanner.getAopBeanDefinition(beanDefSet));
        }


        configObj = ReflectUtil.createInstance(configClass);
        injectConfigObj();
        log.debug(BLUE + "end processing configuration" + RESET);
    }


    private void createComponents() {
        log.debug(BLUE + "start creating components" + RESET);

        for (var def : processorDefs) {
            var processor = createBean(def);
            postProcessors.add((BeanPostProcessor) processor);
            log.debug("proactively created the processor: {}", processor);
        }

        for(var def : remainBeanDefSet) {
            var component = createBean(def);
            log.debug("proactively created the component: {}", component);
        }
        for(var method : remainBeanMethods) {
            var component = createBean(method);
            log.debug("proactively created the component: {}", component);
        }
        log.debug(BLUE + "end creating components" + RESET);
    }

    private Object getBeanInside(Class<?> clazz) {
        if (BeanJudge.isMapper(clazz)) {
            return getMapper(clazz);
        }

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
            var origin = prototypeCache.getOrDefault(definition, objectFactory.getBean(definition.clazz));
            return lifeCycle.makeLifeCycle(origin, definition);
        }
        // definition.scope == ScopeType.SINGLETON
        var instance = singletonObjects.getOrDefault(definition, earlySingletonObjects.getOrDefault(definition, null));
        if (instance == null) {
            remainBeanDefSet.remove(definition);
            var origin = objectFactory.getBean(definition.clazz);
            instance = lifeCycle.makeLifeCycle(origin, definition);
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
        var instance = objectFactory.getBean(method, configObj);
        remainBeanMethods.remove(method);
        var def = BeanDefinitionBuilder.getMethodBeanDef(method);
        registerBean(def, instance);
        return instance;
    }

    private Object createBeanFromFactoryBean(Class<?> clazz) {
        log.debug("want to get Bean from FactoryBean, which class: {}", clazz);
        var def = factoryBeanMap.get(clazz);
        FactoryBean<?> factoryBean;
        if (def != null) {
            factoryBean = (FactoryBean<?>) createBean(def);
        } else {
            var factoryBeanMethod = factoryMethodBeanMap.get(clazz);
            if (factoryBeanMethod == null) return null;
            factoryBean = (FactoryBean<?>) createBean(factoryBeanMethod);
        }

        boolean isSingleton = factoryBean.isSingleton();
        var bean = factoryBean.getObject();
        if (isSingleton) {
            factoryBeanMap.remove(clazz);
            var beanDef = new BeanDefinition(clazz, bean.getClass(),
                    StringUtil.lowerFirst(clazz.getSimpleName()), ScopeType.SINGLETON);
            registerBean(beanDef, bean);
        }
        return bean;
    }

    private void injectBean(Object bean, BeanDefinition definition) {
        if (!ReflectUtil.hasAnnotationOnField(definition.clazz, Autowired.class)) return;
        log.debug("want to inject Bean: {}, which definition: {}", bean, definition);
        boolean isPrototype = definition.scope == ScopeType.PROTOTYPE;
        var targetCache = isPrototype ? prototypeCache : earlySingletonObjects;

        if (!targetCache.containsKey(definition)) {
            log.debug("put {} into cache", bean);
            targetCache.put(definition, bean);
            objectFactory.injectBean(bean);
            targetCache.remove(definition);
            log.debug("remove {} from cache", bean);
        }
    }

    private Object proxyBean(Object bean, BeanDefinition definition) {
        if (!enableProxy) return bean;

        var keyType = definition.keyType;
        var def = aopDefMap.get(keyType);
        if (def == null) return bean;

        var aopObj = createBean(def);
        log.debug("will proxy bean: {} with {}", bean, aopObj);
        return proxyFactory.makeProxy(bean, aopObj);
    }

    private Object getMapper(Class<?> clazz) {
        log.debug("want to get Mapper which class: {}", clazz.getName());
        var sqlSessionFactory = getBean(SqlSessionFactory.class);
        if (!hasAddMappers) {
            mapperPaths.forEach(sqlSessionFactory.getConfiguration()::addMappers);
            hasAddMappers = true;
        }
        var mapper = sqlSessionFactory.openSession(true).getMapper(clazz);
        var def = new BeanDefinition(clazz, mapper.getClass(), StringUtil.lowerFirst(clazz.getSimpleName()), ScopeType.SINGLETON);
        registerBean(def, mapper);
        return mapper;
    }

    private void injectConfigObj() {
        valuesFactory.setPropsValue(configObj);
        objectFactory.injectBean(configObj);
    }

    public <T> T getBean(Class<T> clazz) {
        return (T) getBeanInside(clazz);
    }

    public Object getBean(String name) {
        log.debug("want to get Bean which name: {}", name);
        var def = getBeanDefinition(name);
        if (def != null) {
            return createBean(def);
        } else {
            var method = getBeanMethod(name);
            if (method == null) throw new BeanNotFoundException(name);
            return createBean(method);
        }
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
        return beanDefSet.stream().filter(def -> def.keyType == clazz).findFirst().orElse(null);
    }

    public BeanDefinition getBeanDefinition(String name) {
        return beanDefSet.stream().filter(def -> def.name.equals(name)).findFirst().orElse(null);
    }

    public BeanDefinition[] getBeanDefinition(Predicate<BeanDefinition> predicate) {
        return beanDefSet.stream().filter(predicate).toArray(BeanDefinition[]::new);
    }

    private Method getBeanMethod(Class<?> returnType) {
        return beanMethods.stream()
                .filter(method -> BeanDefinitionBuilder.getKeyType(method) == returnType)
                .findFirst()
                .orElse(null);
    }

    private Method getBeanMethod(String name) {
        return beanMethods.stream()
                .filter(method -> BeanDefinitionBuilder.getName(method).equals(name))
                .findFirst()
                .orElse(null);
    }

    public Set<BeanPostProcessor> getBeanPostProcessors() {
        return this.postProcessors;
    }

    public void logBeanDefs() {
        beanDefSet.forEach(System.out::println);
    }

    public void setOverride(boolean canOverride) {
        this.canOverride = canOverride;
    }

    public static void setLoggerLevel(Level level) {
        Logger logger = (Logger) LoggerFactory.getLogger("com.cppsh1t.crumb");
        logger.setLevel(level);
    }

    public void close() {
        for(var pair : singletonObjects.entrySet()) {
            var def = pair.getKey();
            var bean = pair.getValue();
            lifeCycle.overLifeCycle(bean, def.name);
        }

        singletonObjects.clear();
        beanDefSet.clear();
        remainBeanDefSet.clear();
        beanMethods.clear();
        remainBeanMethods.clear();
        factoryBeanMap.clear();
    }
}
