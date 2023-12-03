package io.avaje.inject.aop;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * Provides method interception such that logic can be provided both before and after method invocation.
 * <p>
 * MethodInterceptor instances are provided by {@link AspectProvider#interceptor(Method, Annotation)}.
 */
@FunctionalInterface
public interface MethodInterceptor {

  /**
   * Implementation can perform before and after invocation logic.
   * <p>
   * If a method interceptor wants to replace the result it does this via {@link Invocation#result(Object)}.
   * This is a little different to traditional method interceptors (i.e. <code>org.org.aopalliance.intercept.MethodInterceptor</code>).
   * It is done this way to handle implied generic return types in the generated source code.
   *
   * @param invocation The invocation being intercepted
   * @throws Throwable If the interception or underlying invocation throws an exception
   */
  void invoke(Invocation invocation) throws Throwable;
}
