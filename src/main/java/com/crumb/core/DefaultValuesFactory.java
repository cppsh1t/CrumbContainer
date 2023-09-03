package com.crumb.core;

import com.crumb.annotation.Value;
import com.crumb.exception.ValueNotFoundException;
import com.crumb.util.ReflectUtil;
import com.crumb.util.YamlUtil;
import lombok.extern.slf4j.Slf4j;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
public class DefaultValuesFactory implements ValuesFactory{

    private final Yaml parser = new Yaml();
    private final Map<String, Object> propMap = new HashMap<>();
    private final ClassLoader classLoader = this.getClass().getClassLoader();


    public DefaultValuesFactory() {
        filePaths.add(defaultPath);
        filePaths.forEach(this::parseYaml);
    }


    private void parseYaml(String path) {
        log.debug("parse yaml: {}", path);
        try (InputStream inputStream = classLoader.getResourceAsStream(path)) {
            if (inputStream == null) return;
            Map<String, Object> linkedMap = parser.load(inputStream);
            propMap.putAll(YamlUtil.castYaml(linkedMap));
        } catch (IOException exception) {
            log.debug("can't find yaml: {}", path);
        }
    }

    @Override
    public void logBanner() {
        try (InputStream inputStream = classLoader.getResourceAsStream("banner.txt")) {
            try (var reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            }
        } catch (IOException exception) {
            //do nothing
        }
    }


    private void setPropValue(Field field, Object target) {
        if (!field.isAnnotationPresent(Value.class)) return;

        String name = field.getDeclaredAnnotation(Value.class).value();
        Object value = getPropValue(name);
        ReflectUtil.setFieldValue(field, target, value);
        log.debug("set value: {} on field: {} from Prop", value, field);
    }


    @Override
    public void setPropsValue(Object bean) {
        var fields = ReflectUtil.getFieldsWithAnnotation(bean.getClass(), Value.class);
        fields.forEach(field -> setPropValue(field, bean));
    }

    @Override
    public Object getPropValue(String name) {
        var instance = propMap.get(name);
        if (instance == null) throw  new ValueNotFoundException(name);
        return instance;
    }

    @Override
    public Object getPropValueNoThrow(String name) {
        return propMap.get(name);
    }

}
