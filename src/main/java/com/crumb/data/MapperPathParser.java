package com.crumb.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MapperPathParser {

    public static List<String> getMapperPaths(Class<?> configClass) {
        var list = new ArrayList<String>();
        if (configClass.isAnnotationPresent(MapperScan.class)) {
            var path = configClass.getAnnotation(MapperScan.class).value();
            list.add(path);
        }

        if (configClass.isAnnotationPresent(MapperScans.class)) {
            var paths = configClass.getAnnotation(MapperScans.class).value();
            list.addAll(Arrays.stream(paths).toList());
        }

        return list;
    }
}
