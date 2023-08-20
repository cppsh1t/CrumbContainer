package com.cppsh1t.crumb.exception;

import java.lang.reflect.Method;
import java.util.Arrays;

public class MethodInvocationException extends RuntimeException{

    public MethodInvocationException(Method method) {
        super("method: " + method.getName() + " can't invoke");
    }

}
