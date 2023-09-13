package com.crumb.exception;

public class ContainerConstructorException extends RuntimeException{

    public ContainerConstructorException() {
        super("Container need a constructor which requires a parameter of type Class.class");
    }
}
