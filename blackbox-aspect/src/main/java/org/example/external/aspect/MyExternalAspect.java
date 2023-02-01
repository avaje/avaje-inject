package org.example.external.aspect;

import io.avaje.inject.aop.Aspect;
import org.example.external.aspect.sub.MyAspectImplementation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Aspect(target = MyAspectImplementation.class)
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MyExternalAspect {
}
