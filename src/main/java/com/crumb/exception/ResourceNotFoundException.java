package com.crumb.exception;

public class ResourceNotFoundException extends RuntimeException{

    public ResourceNotFoundException(String msg) {
        super("资源 {" + msg + "} 未找到");
    }
}
