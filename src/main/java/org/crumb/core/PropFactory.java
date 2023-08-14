package org.crumb.core;

import ch.qos.logback.classic.Logger;
import lombok.extern.slf4j.Slf4j;
import org.crumb.annotation.Values;
import org.crumb.exception.ValueNotFoundException;
import org.crumb.util.ReflectUtil;
import org.crumb.util.StringUtil;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.*;

@Slf4j
public class PropFactory {

    private final Logger logger = (ch.qos.logback.classic.Logger) log;
    private static final Set<String> filePaths = new HashSet<>();
    private final Yaml parser = new Yaml();
    private final List<Map<String, Object>> valuesData = new ArrayList<>();
    private final ClassLoader classLoader = this.getClass().getClassLoader();
    private static String defaultPath = "application.yaml";

    public static void setDefaultPath(String path) {
        defaultPath = path;
    }

    public PropFactory() {
        logger.setLevel(LoggerManager.currentLevel);
        filePaths.add(defaultPath);
        filePaths.forEach(this::parseYaml);
    }

    public static void addFilePath(String... paths) {
        filePaths.addAll(Arrays.asList(paths));
    }

    private void parseYaml(String path) {
        logger.debug("parse yaml: " + path);
        try (InputStream inputStream = classLoader.getResourceAsStream(path)) {
            if (inputStream == null) return;
            Map<String, Object> data = parser.load(inputStream);
            valuesData.add(data);
            logger.debug("and the prop: " + data);
        } catch (IOException exception) {
            logger.debug("can't find yaml: " + path);
        }
    }

    public void setPropsValue(Object bean) {
        var fields = ReflectUtil.getFieldsWithAnnotation(bean.getClass(), Values.class);
        fields.forEach(field -> setPropValue(field, bean));
    }

    private void setPropValue(Field field, Object target) {
        if (!field.isAnnotationPresent(Values.class)) return;

        String name = field.getDeclaredAnnotation(Values.class).value();
        Object value = getPropValue(name);
        ReflectUtil.setFieldValue(field, target, value);
        logger.debug("set value: " + value + " on field: " + field + " from Prop");
    }

    public Object getPropValue(String names) {
        String[] nameArray = names.split("\\.");
        String firstName = nameArray[0];
        nameArray = StringUtil.removeFirstElement(nameArray);

        Object firstValue = valuesData.stream()
                .filter(Objects::nonNull)
                .map(map -> map.get(firstName))
                .filter(Objects::nonNull)
                .findFirst().orElseThrow(() -> new ValueNotFoundException(names));
        if (!(firstValue instanceof Map)) {
            return firstValue;
        }

        Map<String, Object> query = (Map<String, Object>) firstValue;
        for(String name : nameArray) {
            Object value = Optional.ofNullable(query.get(name)).orElseThrow(() -> new ValueNotFoundException(names)) ;
            if (value instanceof Map) {
                query = (Map<String, Object>) value;
            } else {
                return value;
            }
        }

        throw new ValueNotFoundException(names);
    }

    public Object getPropValueNoThrow(String names) {
        String[] nameArray = names.split("\\.");
        String firstName = nameArray[0];
        nameArray = StringUtil.removeFirstElement(nameArray);

        Object firstValue = valuesData.stream()
                .filter(Objects::nonNull)
                .map(map -> map.get(firstName))
                .filter(Objects::nonNull)
                .findFirst().orElse(null);
        if (!(firstValue instanceof Map)) {
            return firstValue;
        }

        Map<String, Object> query = (Map<String, Object>) firstValue;
        for(String name : nameArray) {
            Object value = Optional.ofNullable(query.get(name)).orElse(null) ;
            if (value instanceof Map) {
                query = (Map<String, Object>) value;
            } else {
                return value;
            }
        }

        return null;
    }

}
