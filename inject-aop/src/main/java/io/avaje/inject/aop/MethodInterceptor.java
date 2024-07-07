package io.avaje.inject.aop;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * Provides method interception logic that can be executed before, during, and after a method invocation.
 * <p>
 * MethodInterceptor instances are provided by {@link AspectProvider#interceptor(Method, Annotation)}.
 */
@FunctionalInterface
public interface MethodInterceptor {

  /**
   * Intercepts the method invocation.
   * <p>
   * If the interceptor wants to replace the result, it can be done via {@link Invocation#result(Object)}.
   *
   * @param invocation The invocation being intercepted
   * @throws Throwable If the interception or underlying invocation throws an exception
   */
  void invoke(Invocation invocation) throws Throwable;
}
