package io.avaje.inject;

/**
 * Provides the {@code MethodInterceptor} for the given aspect.
 *
 * @param <T> The aspect annotation
 */
@FunctionalInterface
public interface AssistInjector<T> {

  /** Return the method interceptor to use for the given method and aspect annotation. */
  T inject(Object... dependencies);
}
