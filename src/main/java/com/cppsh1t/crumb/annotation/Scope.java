package com.cppsh1t.crumb.annotation;

import com.cppsh1t.crumb.definition.ScopeType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Scope {

    ScopeType value() default ScopeType.SINGLETON;
}
