package com.crumb.core;

public interface ValuesFactory {

    void logBanner();

    void setPropsValue(Object bean);

    Object getPropValue(String names);

    Object getPropValueNoThrow(String names);

}
