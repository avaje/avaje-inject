package org.example.myapp.resilience4j;

import io.avaje.inject.AspectProvider;
import io.avaje.inject.Fallback;
import io.avaje.inject.Invocation;
import io.avaje.inject.MethodInterceptor;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import jakarta.inject.Singleton;

import java.lang.reflect.Method;
import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

@Singleton
public class RetryProvider implements AspectProvider<MyRetry> {

  @Override
  public MethodInterceptor interceptor(Method method, MyRetry retryAnnotation) {
    // build and return the Retry MethodInterceptor
    RetryConfig.Builder<Object> builder = RetryConfig.custom();

    if (retryAnnotation.maxAttempts() > 0) {
      builder.maxAttempts(retryAnnotation.maxAttempts());
    }
    if (!retryAnnotation.waitDuration().isEmpty()) {
      try {
        Duration duration = Duration.parse(retryAnnotation.waitDuration());
        builder.waitDuration(duration);
      } catch (DateTimeParseException e) {
        e.printStackTrace();
      }

    }
    RetryConfig retryConfig = RetryConfig.ofDefaults();
    RetryConfig config = builder.build();
    io.github.resilience4j.retry.Retry retry = io.github.resilience4j.retry.Retry.of("id", config);

    Fallback fallback = fallback(method, retryAnnotation);

    return fallback == null ?  new RetryInterceptor(retry) : new RetryFallbackInterceptor(retry, fallback);
  }

  private Fallback fallback(Method method, MyRetry retryAnnotation) {
    if (!retryAnnotation.fallbackMethod().isEmpty()) {
      try {
        return Fallback.find(retryAnnotation.fallbackMethod(), method);
      } catch (NoSuchMethodException e) {
        throw new IllegalStateException(e);
      }
    }
    return null;
  }


  public static AtomicInteger testCounter = new AtomicInteger();

  static class RetryInterceptor implements MethodInterceptor {

    private final io.github.resilience4j.retry.Retry retry;

    public RetryInterceptor(Retry retry) {
      this.retry = retry;
    }

    @Override
    public void invoke(Invocation invocation) throws Throwable {
      testCounter.incrementAndGet();
      Retry.decorateCheckedSupplier(retry, invocation::invoke)
        .apply();
    }
  }

  static class RetryFallbackInterceptor implements MethodInterceptor {

    private final io.github.resilience4j.retry.Retry retry;
    private final Fallback fallback;

    public RetryFallbackInterceptor(Retry retry, Fallback fallback) {
      this.retry = retry;
      this.fallback = fallback;
    }

    @Override
    public void invoke(Invocation invocation) throws Throwable {
      testCounter.incrementAndGet();
      Retry.decorateCheckedSupplier(retry, invocation::invoke)
        .recover((throwable) -> (Supplier<Object>) () -> fallback.invoke(invocation, throwable))
        .apply();
    }
  }

}
