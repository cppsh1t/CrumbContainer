package com.crumb.core;

import ch.qos.logback.classic.Logger;
import com.crumb.annotation.Autowired;
import com.crumb.annotation.EnableAspectProxy;
import com.crumb.annotation.Lazy;
import com.crumb.annotation.Resource;
import com.crumb.beanProcess.BeanPostProcessor;
import com.crumb.builder.BeanDefinitionBuilder;
import com.crumb.data.MapperScanner;
import com.crumb.definition.BeanDefinition;
import com.crumb.definition.BeanJudge;
import com.crumb.definition.ScopeType;
import com.crumb.exception.BeanNotFoundException;
import com.crumb.proxy.DefaultProxyFactory;
import com.crumb.proxy.ProxyFactory;
import com.crumb.util.ClassConverter;
import com.crumb.util.ReflectUtil;
import com.crumb.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.crumb.misc.Color.*;
import static com.crumb.misc.Color.BLUE;


public class AbstractContainer implements Container{

    protected Logger log = (Logger) LoggerFactory.getLogger(this.getClass());

    //region Flags
    protected boolean canOverride = false;
    protected boolean enableProxy = false;
    protected boolean hasAddMappers = false;
    //endregion

    //region Configuration
    protected final Class<?> configClass;
    protected Object configObj;
    protected final List<String> mapperPaths = new ArrayList<>();
    //endregion

    //region ChildrenModules
    protected BeanScanner scanner;
    protected ObjectFactory objectFactory;
    protected ValuesFactory valuesFactory;
    protected ProxyFactory proxyFactory;
    //endregion

    //region BeanDefinitions Holder
    protected final Set<BeanDefinition> beanDefSet = new HashSet<>();
    protected final Set<Method> beanMethods = ConcurrentHashMap.newKeySet();
    protected final Set<BeanDefinition> remainBeanDefSet = ConcurrentHashMap.newKeySet();
    protected final Set<Method> remainBeanMethods = ConcurrentHashMap.newKeySet();
    protected final Map<Class<?>, BeanDefinition> aopDefMap = new ConcurrentHashMap<>();
    //endregion

    //region Singleton and Prototype
    protected final Map<BeanDefinition, Object> singletonObjects = new ConcurrentHashMap<>();
    protected final Map<BeanDefinition, Object> earlySingletonObjects = new ConcurrentHashMap<>();
    protected final Map<BeanDefinition, Object> prototypeCache = new ConcurrentHashMap<>();
    //endregion

    //region FactoryBean Map
    protected final Map<Class<?>, BeanDefinition> factoryBeanMap = new ConcurrentHashMap<>();
    protected final Map<Class<?>, Method> factoryMethodBeanMap = new ConcurrentHashMap<>();
    //endregion

    //region PostProcessor
    protected final Set<BeanDefinition> processorDefs = new HashSet<>();
    protected final Set<BeanPostProcessor> postProcessors = new HashSet<>();
    //endregion

    //region BeanLifeCycle
    protected BeanLifeCycle lifeCycle;
    //endregion

    public AbstractContainer(Class<?> configClass) {
        this.configClass = configClass;
        if (configClass.isAnnotationPresent(EnableAspectProxy.class)) {
            enableProxy = true;
        }
        initContainerProp();
        logBanner();
        initContainer();
    }

    protected void initContainerProp() {
        scanner = new DefaultBeanScanner();
        objectFactory = new DefaultObjectFactory(this::getBeanInside, this::getBean);
        valuesFactory = new DefaultValuesFactory();
        proxyFactory = new DefaultProxyFactory(this::getBeanInside);
        lifeCycle = new DefaultBeanLifeCycle(valuesFactory::setPropsValue, this::injectBean,
                this::proxyBean, this.postProcessors);
    }

    private void initContainer() {
        processConfig();
        createComponents();
        log.info(GREEN + "Successfully started crumbContainer" + RESET);
    }

    private void processConfig() {
        log.debug(BLUE + "start processing configuration" + RESET);
        mapperPaths.addAll(MapperScanner.getMapperPaths(configClass));

        beanDefSet.addAll(scanner.getBeanDefinition(configClass));
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

    protected final Object getBeanInside(Class<?> clazz) {
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

    protected final void injectBean(Object bean, BeanDefinition definition) {
        if (!ReflectUtil.hasAnnotationsOnField(definition.clazz, new Class[]{Autowired.class, Resource.class})) return;
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

    protected final Object proxyBean(Object bean, BeanDefinition definition) {
        if (!enableProxy) return bean;

        var keyType = definition.keyType;
        var def = aopDefMap.get(keyType);
        if (def == null) return bean;

        var aopObj = createBean(def);
        log.debug("will proxy bean: {} with {}", bean, aopObj);
        return proxyFactory.makeProxy(bean, aopObj);
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

    private void injectConfigObj() {
        valuesFactory.setPropsValue(configObj);
        objectFactory.injectBean(configObj);
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

    @Override
    public final <T> T getBean(Class<T> clazz) {
        return (T) getBeanInside(clazz);
    }

    @Override
    public final Object getBean(String name) {
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

    @Override
    public final boolean registerBean(BeanDefinition definition, Object object) {
        if (!singletonObjects.containsKey(definition) || canOverride)  {
            singletonObjects.put(definition, object);
            beanDefSet.add(definition);
            log.debug("register Bean: {}, which definition: {}", object, definition);
            return true;
        } else return false;
    }

    @Override
    public final BeanDefinition getBeanDefinition(Class<?> clazz) {
        return beanDefSet.stream().filter(def -> def.keyType == clazz).findFirst().orElse(null);
    }

    @Override
    public final BeanDefinition getBeanDefinition(String name) {
        return beanDefSet.stream().filter(def -> def.name.equals(name)).findFirst().orElse(null);
    }

    @Override
    public final BeanDefinition[] getBeanDefinition(Predicate<BeanDefinition> predicate) {
        return beanDefSet.stream().filter(predicate).toArray(BeanDefinition[]::new);
    }

    @Override
    public final Set<BeanPostProcessor> getBeanPostProcessors() {
        return this.postProcessors;
    }

    @Override
    public void logBeanDefs() {
        beanDefSet.forEach(System.out::println);
    }

    @Override
    public final void setOverride(boolean canOverride) {
        this.canOverride = canOverride;
    }

    @Override
    public final void close() {
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

    public void logBanner() {
        valuesFactory.logBanner();
    }
}
