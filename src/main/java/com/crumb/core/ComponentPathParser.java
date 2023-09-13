package com.crumb.core;

import com.crumb.annotation.ComponentScan;
import com.crumb.annotation.ComponentScans;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Slf4j
public class ComponentPathParser {

    public static Set<String> getComponentScanPath(Class<?> clazz) {
        var scanPaths = new HashSet<String>();
        if (clazz.isAnnotationPresent(ComponentScan.class)) {
            var path = clazz.getDeclaredAnnotation(ComponentScan.class).value();
            scanPaths.add(path);
            log.debug("Get componentScanPath: {}", path);
        }
        if (clazz.isAnnotationPresent(ComponentScans.class)) {
            var paths = clazz.getDeclaredAnnotation(ComponentScans.class).value();
            scanPaths.addAll(Arrays.asList(paths));
            Arrays.stream(paths).forEach(p -> log.debug("Get componentScanPath: {}", p));
        }
        return scanPaths;
    }
}
