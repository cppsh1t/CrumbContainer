package com.crumb.util;

import java.util.HashMap;
import java.util.Map;

public class YamlUtil {

    public static Map<String, Object> castYaml(Map<String, Object> source) {
        var resultMap = new HashMap<String, Object>();

        for(var pair : source.entrySet()) {
            String key = pair.getKey();
            Object value = pair.getValue();
            if (value instanceof Map<?, ?> ) {
                var map = (Map<String, Object>) value;
                resultMap.putAll(castYaml(map, key));
            } else {
                resultMap.put(key, value);
            }
        }

        return resultMap;
    }

    public static Map<String, Object> castYaml(Map<String, Object> source, String firstName) {
        var resultMap = new HashMap<String, Object>();

        for(var pair : source.entrySet()) {
            String key = pair.getKey();
            Object value = pair.getValue();
            if (value instanceof Map<?, ?> ) {
                var map = (Map<String, Object>) value;
                resultMap.putAll(castYaml(map, firstName + "." + key));
            } else {
                resultMap.put(firstName + "." + key, value);
            }
        }

        return resultMap;
    }
}
