package com.crumb.exception;

public class DefaultConstructorException extends RuntimeException{

    public DefaultConstructorException(Class<?> clazz) {
        super("Cant invoke the norArgs-Constructor of " + clazz.getName());
    }
}
