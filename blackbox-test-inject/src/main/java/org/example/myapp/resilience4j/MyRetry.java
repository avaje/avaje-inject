package org.example.myapp.resilience4j;

import io.avaje.inject.aop.Aspect;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Aspect
@Retention(RetentionPolicy.RUNTIME)
public @interface MyRetry {

  String fallbackMethod() default "";

  int maxAttempts() default -1;

  String waitDuration() default "";

}
