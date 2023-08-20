package com.crumb.exception;

import java.lang.reflect.Constructor;
import java.util.Arrays;

public class CreateInstanceException extends RuntimeException{

    public CreateInstanceException(Constructor<?> con, Object... args) {
        super("can't invoke the " + con.getName() + " with parameter: " + Arrays.toString(args));
    }
}
