package com.cppsh1t.crumb.core;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.cppsh1t.crumb.builder.BeanDefinitionBuilder;
import com.cppsh1t.crumb.data.MapperScanner;
import com.cppsh1t.crumb.data.SqlSessionFactoryBean;
import com.cppsh1t.crumb.definition.BeanDefinition;
import com.cppsh1t.crumb.exception.BeanNotFoundException;
import com.cppsh1t.crumb.proxy.ProxyFactory;
import com.cppsh1t.crumb.util.ClassConverter;
import com.cppsh1t.crumb.util.ReflectUtil;
import lombok.extern.slf4j.Slf4j;
import com.cppsh1t.crumb.annotation.Autowired;
import com.cppsh1t.crumb.annotation.EnableAspectProxy;
import com.cppsh1t.crumb.annotation.Lazy;
import com.cppsh1t.crumb.definition.ScopeType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.cppsh1t.crumb.misc.Color.*;

@Slf4j
public class CrumbContainer implements BeanFactory {

    private final List<String> mapperPaths = new ArrayList<>();

    private boolean canOverride = false;
    private boolean enableProxy = false;
    private boolean hasAddMappers = false;

    private final BeanScanner scanner = new BeanScanner();
    private final ObjectFactory objectFactory = new ObjectFactory(this::getBeanInside);
    private final ValuesFactory valuesFactory = new ValuesFactory();
    private final BeanIniter beanIniter = new BeanIniter();
    private final BeanDestroyer beanDestroyer = new BeanDestroyer();
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
        var classFiles = scanPaths.stream()
                .map(scanner::getComponentFile)
                .flatMap(List::stream)
                .collect(Collectors.toList());
        beanDefSet.addAll(scanner.getBeanDefinition(classFiles));
        factoryBeanMap.putAll(scanner.getFactoryBeanDefinition(beanDefSet));
        remainBeanDefSet.addAll(beanDefSet.stream().filter(def -> def.scope == ScopeType.SINGLETON
                && !def.clazz.isAnnotationPresent(Lazy.class)).collect(Collectors.toSet()));

        beanMethods.addAll(scanner.getBeanMethod(configClass));
        factoryMethodBeanMap.putAll(scanner.getFactoryBeanMethods(beanMethods));
        remainBeanMethods.addAll(beanMethods.stream()
                .filter(method -> !method.isAnnotationPresent(Lazy.class)).collect(Collectors.toSet()));

        if (enableProxy) {
            aopDefMap.putAll(scanner.getAopBeanDefinition(beanDefSet));
        }


        configObj = ReflectUtil.createInstance(configClass);
        injectConfigObj();
        log.debug(BLUE + "end processing configuration" + RESET);
    }


    private void createComponents() {
        log.debug(BLUE + "start creating components" + RESET);
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
        if (clazz.isAnnotationPresent(Mapper.class)) {
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
            valuesFactory.setPropsValue(origin);
            var instance = proxyBean(origin, definition);
            injectBean(origin, definition);
            beanIniter.initBean(origin);
            return instance;
        }
        // definition.scope == ScopeType.SINGLETON
        var instance = singletonObjects.getOrDefault(definition, earlySingletonObjects.getOrDefault(definition, null));
        if (instance == null) {
            remainBeanDefSet.remove(definition);
            instance = objectFactory.getBean(definition.clazz);
            valuesFactory.setPropsValue(instance);
            instance = proxyBean(instance, definition);
            injectBean(instance, definition);
            beanIniter.initBean(instance);
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
            var beanDef = new BeanDefinition(clazz, bean.getClass(), ScopeType.SINGLETON);
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
        var sqlSessionFactory = getBean(SqlSessionFactory.class);
        if (!hasAddMappers) {
            mapperPaths.forEach(sqlSessionFactory.getConfiguration()::addMappers);
        }
        var mapper = sqlSessionFactory.openSession(true).getMapper(clazz);
        registerBean(new BeanDefinition(clazz, mapper.getClass(), ScopeType.SINGLETON), mapper);
        return mapper;
    }

    private void injectConfigObj() {
        valuesFactory.setPropsValue(configObj);
        objectFactory.injectBean(configObj);
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
        return beanDefSet.stream().filter(def -> def.keyType == clazz).findFirst().orElse(null);
    }

    private Method getBeanMethod(Class<?> returnType) {
        return beanMethods.stream()
                .filter(method -> BeanDefinitionBuilder.getKeyType(method) == returnType)
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
        Logger logger = (Logger) LoggerFactory.getLogger("com.cppsh1t.crumb");
        logger.setLevel(level);
    }

    public void close() {
        singletonObjects.values().forEach(beanDestroyer::destroyBean);
        singletonObjects.clear();
        beanDefSet.clear();
        remainBeanDefSet.clear();
        beanMethods.clear();
        remainBeanMethods.clear();
        factoryBeanMap.clear();
    }
}
