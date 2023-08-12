package org.crumb.container;


import org.crumb.annotation.*;
import org.crumb.definition.BeanDefinition;
import org.crumb.exception.ResourceNotFoundException;
import org.crumb.util.FileUtil;
import org.crumb.util.ReflectUtil;
import org.crumb.util.StringUtil;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class BeanScanner {

    private final ClassLoader classLoader = this.getClass().getClassLoader();


    public Set<String> getComponentScanPath(Class<?> clazz) {
        var scanPaths = new HashSet<String>();
        if (clazz.isAnnotationPresent(ComponentScan.class)) {
            var path = clazz.getDeclaredAnnotation(ComponentScan.class).value();
            scanPaths.add(path);
        }
        if (clazz.isAnnotationPresent(ComponentScans.class)) {
            var paths = clazz.getDeclaredAnnotation(ComponentScans.class).value();
            scanPaths.addAll(Arrays.asList(paths));
        }
        return scanPaths;
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
        } else if (file.isDirectory()) {
            List<File> childrenFiles = FileUtil.getAllFiles(file);
            files.addAll(childrenFiles);
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

            try {
                Class<?> clazz = classLoader.loadClass(className);
                if (!clazz.isAnnotationPresent(Component.class)) return;

                ScopeType scope = clazz.isAnnotationPresent(Scope.class) ? clazz.getDeclaredAnnotation(Scope.class).value()
                        : ScopeType.SINGLETON;

                var definition = new BeanDefinition(clazz, scope);
                definitions.add(definition);
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
                        .collect(Collectors.toList())
                : new ArrayList<>();
    }

    public Map<Class<?>, BeanDefinition> getFactoryBeanDefinition(Set<BeanDefinition> definitions) {
        var map = new HashMap<Class<?>, BeanDefinition>();
        definitions.forEach(def -> {
            if (FactoryBean.class.isAssignableFrom(def.clazz)) {
                var beanClass = ReflectUtil.getFirstParamFromGenericInterface(def.clazz, FactoryBean.class);
                map.put(beanClass, def);
            }
        });
        return map;
    }

}
