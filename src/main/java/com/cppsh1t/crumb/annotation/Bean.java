package com.cppsh1t.crumb.annotation;

import com.cppsh1t.crumb.definition.Empty;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Bean {

    Class<?> value() default Empty.class;
}
