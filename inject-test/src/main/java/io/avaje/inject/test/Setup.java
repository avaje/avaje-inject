package io.avaje.inject.test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specify a method in a test that takes a BeanScopeBuilder. The method is used to setup the test
 * using the BeanScopeBuilder prior to the BeanScope being built.
 * <p>
 * The method is expected to only have a single parameter that is of type BeanScopeBuilder.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Setup {

}
