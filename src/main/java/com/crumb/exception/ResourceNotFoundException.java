package com.crumb.exception;

public class ResourceNotFoundException extends RuntimeException{

    public ResourceNotFoundException(String msg) {
        super("resource {" + msg + "} cant find");
    }
}
