package com.crumb.exception;

public class MainConfigurationNotFoundException extends RuntimeException{

    public MainConfigurationNotFoundException() {
        super("can't find a class annotated with MainConfiguration");
    }
}
