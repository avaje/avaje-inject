package io.avaje.inject.aop;

import java.lang.reflect.Method;

/**
 * A fallback or recovery method used with Aspects.
 * <p>
 * This isn't strictly required but more a helper to make it easier for aspects
 * that want to use a fallback or recovery method.
 */
public interface Fallback {

  /**
   * Find and return the fallback given the name and original method.
   * <p>
   * This will first try and find the method for the name that has the same parameters as
   * the method but additionally takes a {@code Throwable} as the last parameter. If no
   * matching method is found it then looks for the method with the name that just has the
   * matching parameters (and not the additional throwable).
   *
   * @param name   The name of the fallback method
   * @param method The original method which we match to using argument types.
   * @return The fallback
   * @throws NoSuchMethodException When no matching fallback method is found
   */
  static Fallback find(String name, Method method) throws NoSuchMethodException {
    return FallbackFinder.find(name, method);
  }

  /**
   * Invoke the fallback method given the invocation and exception.
   *
   * @param call            The invocation being executed
   * @param sourceException The exception thrown that triggers the use of the fallback method
   */
  Object invoke(Invocation call, Throwable sourceException);
}
