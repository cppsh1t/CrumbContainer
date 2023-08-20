package com.cppsh1t.crumb.core;

import com.cppsh1t.crumb.exception.ValueNotFoundException;
import com.cppsh1t.crumb.util.ReflectUtil;
import com.cppsh1t.crumb.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import com.cppsh1t.crumb.annotation.Value;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
public class PropFactory {

    private static final Set<String> filePaths = new HashSet<>();
    private final Yaml parser = new Yaml();
    private final List<Map<String, Object>> valuesData = new ArrayList<>();
    private final ClassLoader classLoader = this.getClass().getClassLoader();
    private static String defaultPath = "application.yaml";

    public static void setDefaultPath(String path) {
        defaultPath = path;
    }

    public PropFactory() {
        filePaths.add(defaultPath);
        filePaths.forEach(this::parseYaml);
    }

    public static void addFilePath(String... paths) {
        filePaths.addAll(Arrays.asList(paths));
    }

    public void logBanner() {
        try (InputStream inputStream = classLoader.getResourceAsStream("banner.txt");
             var reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));) {
            String line = null;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            };
        } catch (IOException exception) {
            //do nothing
        }
    }

    private void parseYaml(String path) {
        log.debug("parse yaml: {}", path);
        try (InputStream inputStream = classLoader.getResourceAsStream(path)) {
            if (inputStream == null) return;
            Map<String, Object> data = parser.load(inputStream);
            valuesData.add(data);
            log.debug("add the prop: {}", data);
        } catch (IOException exception) {
            log.debug("can't find yaml: {}", path);
        }
    }

    public void setPropsValue(Object bean) {
        var fields = ReflectUtil.getFieldsWithAnnotation(bean.getClass(), Value.class);
        fields.forEach(field -> setPropValue(field, bean));
    }

    private void setPropValue(Field field, Object target) {
        if (!field.isAnnotationPresent(Value.class)) return;

        String name = field.getDeclaredAnnotation(Value.class).value();
        Object value = getPropValue(name);
        ReflectUtil.setFieldValue(field, target, value);
        log.debug("set value: {} on field: {} from Prop", value, field);
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
