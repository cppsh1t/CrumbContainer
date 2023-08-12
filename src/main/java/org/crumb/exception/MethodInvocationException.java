package org.crumb.exception;

import java.lang.reflect.Method;

public class MethodInvocationException extends RuntimeException{

    public MethodInvocationException(Method method) {
        super("method: " + method.getName() + " can't invoke");
    }
}
