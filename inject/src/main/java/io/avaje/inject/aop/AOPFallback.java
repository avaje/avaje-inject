package io.avaje.inject.aop;

import static java.lang.annotation.ElementType.METHOD;

import java.lang.annotation.*;

/** Marks a method as a recovery method for an AOP operation. */
@Target(METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AOPFallback {

  /** the name of the target method to recover for. */
  String value();
}
