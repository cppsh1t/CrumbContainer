package com.crumb.core;


import com.crumb.builder.BeanDefinitionBuilder;
import com.crumb.definition.BeanDefinition;
import com.crumb.definition.BeanJudge;
import com.crumb.util.FileUtil;
import com.crumb.util.ReflectUtil;
import com.crumb.util.StringUtil;
import com.crumb.annotation.*;
import lombok.extern.slf4j.Slf4j;
import com.crumb.exception.ResourceNotFoundException;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class BeanScanner {

    private final ClassLoader classLoader = this.getClass().getClassLoader();

    public Set<String> getComponentScanPath(Class<?> clazz) {
        var scanPaths = new HashSet<String>();
        if (clazz.isAnnotationPresent(ComponentScan.class)) {
            var path = clazz.getDeclaredAnnotation(ComponentScan.class).value();
            scanPaths.add(path);
            log.debug("get componentScanPath: {}", path);
        }
        if (clazz.isAnnotationPresent(ComponentScans.class)) {
            var paths = clazz.getDeclaredAnnotation(ComponentScans.class).value();
            scanPaths.addAll(Arrays.asList(paths));
            Arrays.stream(paths).forEach(p -> log.debug("get componentScanPath: {}", p));
        }
        return scanPaths;
    }


    public List<File> getComponentFiles(Set<String> paths) {
        return paths.stream()
                .map(this::getComponentFile)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    public List<File> getComponentFile(String scanPath) {
        scanPath = scanPath.replace(".", "/");
        URL resource = classLoader.getResource(scanPath);
        boolean isSingleClass = false;

        // 是单个类
        if (resource == null) {
            scanPath = scanPath + ".class";
            resource = classLoader.getResource(scanPath);
            isSingleClass = true;
        }

        // 再判断一下，可能是个非法路径
        if (resource == null) {
            throw new ResourceNotFoundException(scanPath);
        }
        var files = new ArrayList<File>();
        File file = new File(resource.getFile());

        if (isSingleClass) {
            files.add(file);
            log.debug("get componentFile: {}", file);
        } else if (file.isDirectory()) {
            List<File> childrenFiles = FileUtil.getAllFiles(file);
            files.addAll(childrenFiles);
            childrenFiles.forEach(f -> log.debug("get componentFile: {}", f));
        }
        return files;
    }

    public List<BeanDefinition> getBeanDefinition(List<File> files) {
        var definitions = new ArrayList<BeanDefinition>();
        files.forEach(file -> {
            String fileName = file.getAbsolutePath();
            if (!fileName.endsWith(".class")) return;

            String packName = StringUtil.getPackageName(fileName);
            String className = fileName.substring(fileName.indexOf(packName), fileName.indexOf(".class"));
            className = className.replace("\\", ".");

            if (className.contains("WEB-INF.classes.")) {
                className = className.replace("WEB-INF.classes.", "");
            }

            try {
                Class<?> clazz = classLoader.loadClass(className);
                if (!BeanJudge.isComponent(clazz)) return;

                var definition = BeanDefinitionBuilder.getComponentDef(clazz);
                definitions.add(definition);
                log.debug("get beanDefinition: {}", definition);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        });
        return definitions;
    }

    public List<Method> getBeanMethod(Class<?> configurationClass) {
        return configurationClass.isAnnotationPresent(Configuration.class)
                ? Arrays.stream(configurationClass.getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(Bean.class)
                        && method.getReturnType() != void.class)
                .peek(method -> log.debug("get beanMethod: {}", method))
                .collect(Collectors.toList())
                : new ArrayList<>();
    }

    public Map<Class<?>, BeanDefinition> getFactoryBeanDefinition(Set<BeanDefinition> definitions) {
        var map = new HashMap<Class<?>, BeanDefinition>();
        definitions.forEach(def -> {
            if (FactoryBean.class.isAssignableFrom(def.clazz)) {
                var beanClass = ReflectUtil.getFirstParamFromGenericInterface(def.clazz, FactoryBean.class);
                log.debug("get factoryBeanDefinition: {}, which getObjectType: {}", def, beanClass.getName());
                map.put(beanClass, def);
            }
        });
        return map;
    }

    public Map<Class<?>, Method> getFactoryBeanMethods(Set<Method> methods) {
        var map = new HashMap<Class<?>, Method>();
        methods.forEach(method -> {
            var clazz = method.getReturnType();
            if (FactoryBean.class.isAssignableFrom(clazz)) {
                var beanClass = ReflectUtil.getFirstParamFromGenericInterface(clazz, FactoryBean.class);
                log.debug("get factoryBean which getObjectType: {}", beanClass.getName());
                map.put(beanClass, method);
            }
        });
        return map;
    }

    public Map<Class<?>, BeanDefinition> getAopBeanDefinition(Set<BeanDefinition> definitions) {
        var map = new HashMap<Class<?>, BeanDefinition>();
        definitions.stream().filter(def -> def.clazz.isAnnotationPresent(Aspect.class))
                .peek(def -> log.debug("get AopBeanDefinition: {}", def))
                .forEach(def -> map.put(def.clazz.getAnnotation(Aspect.class).value(), def));
        return map;
    }

}
