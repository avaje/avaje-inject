package io.avaje.inject;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * Provides method interception such that logic can be provided both before and after method invocation.
 * <p>
 * MethodInterceptor instances are provided by {@link AspectProvider#interceptor(Method, Annotation)}.
 */
public interface MethodInterceptor {

  /**
   * Implementation can perform before and after invocation logic.
   *
   * @param invocation The invocation being intercepted
   * @throws Throwable If the interception or underlying invocation throws an exception
   */
  void invoke(Invocation invocation) throws Throwable;
}
