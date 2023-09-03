package com.crumb.annotation;

import com.crumb.core.AbstractContainer;
import com.crumb.core.AutoContainer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SelectContainer {

    Class<? extends AbstractContainer> value() default AutoContainer.class;
}
