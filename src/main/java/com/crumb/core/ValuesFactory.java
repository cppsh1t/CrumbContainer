package com.crumb.core;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public interface ValuesFactory {

    Set<String> filePaths = new HashSet<>();
    String defaultPath = "application.yaml";
    static void addFilePath(String... paths) {
        filePaths.addAll(Arrays.asList(paths));
    }

    void logBanner();

    void setPropsValue(Object bean);

    Object getPropValue(String names);

    Object getPropValueNoThrow(String names);

}
