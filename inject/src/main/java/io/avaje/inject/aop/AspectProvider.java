package io.avaje.inject.aop;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * Provides the {@code MethodInterceptor} for the given aspect.
 *
 * @param <T> The aspect annotation
 */
public interface AspectProvider<T extends Annotation> {

  /**
   * Return the method interceptor to use for the given method and aspect annotation.
   */
  MethodInterceptor interceptor(Method method, T aspectAnnotation);
}
