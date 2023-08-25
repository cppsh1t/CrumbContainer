package com.crumb.core;

import com.crumb.annotation.MainConfiguration;
import com.crumb.exception.MainConfigurationNotFoundException;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;


public class MainContainer {
    private static Container instance;

    public static Container getContainer() {
        if (instance == null) {
            // 创建一个ClassGraph对象
            ClassGraph classGraph = new ClassGraph();
            classGraph.enableAnnotationInfo();

            // 扫描所有的类
            ScanResult scanResult = classGraph.scan();
            // 获取所有的类
            var configClass = scanResult.getClassesWithAnnotation(MainConfiguration.class).get(0).loadClass();

            if (configClass == null) throw new MainConfigurationNotFoundException();
            instance = new CrumbContainer(configClass);
        }
        return instance;
    }

}
