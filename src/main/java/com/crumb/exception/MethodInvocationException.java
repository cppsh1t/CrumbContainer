package com.crumb.exception;

import java.lang.reflect.Method;
import java.util.Arrays;

public class MethodInvocationException extends RuntimeException{

    public MethodInvocationException(Method method) {
        super("Method: " + method.getName() + " can't invoke");
    }

}
