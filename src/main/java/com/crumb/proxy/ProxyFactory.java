package com.crumb.proxy;

public interface ProxyFactory {

    Object makeProxy(Object origin, Object aopObj);
}
