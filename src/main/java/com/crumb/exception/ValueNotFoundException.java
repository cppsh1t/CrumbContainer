package com.crumb.exception;

public class ValueNotFoundException extends RuntimeException{

    public ValueNotFoundException(Object name) {
        super(name + " is not found in PropFactory");
    }
}
