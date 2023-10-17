package org.example.myapp.resilience4j;

import java.lang.reflect.Method;
import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import io.avaje.inject.Component;
import io.avaje.inject.aop.AspectProvider;
import io.avaje.inject.aop.Fallback;
import io.avaje.inject.aop.Invocation;
import io.avaje.inject.aop.MethodInterceptor;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;

@Component
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
    RetryConfig config = builder.build();
    io.github.resilience4j.retry.Retry retry = io.github.resilience4j.retry.Retry.of("id", config);

    Fallback fallback = fallback(method, retryAnnotation);

    return fallback == null
        ? new RetryInterceptor(retry)
        : new RetryFallbackInterceptor(retry, fallback);
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
      Retry.decorateCheckedRunnable(retry, invocation::invoke).run();
    }
  }

  static class RetryFallbackInterceptor implements MethodInterceptor {

    private final io.github.resilience4j.retry.Retry retry;
    private final Fallback fallback;
    private final Predicate<Throwable> errorPredicate;

    public RetryFallbackInterceptor(Retry retry, Fallback fallback) {
      this.retry = retry;
      this.fallback = fallback;
      this.errorPredicate = retry.getRetryConfig().getExceptionPredicate();
    }

    @Override
    public void invoke(Invocation invocation) throws Throwable {
      testCounter.incrementAndGet();
      try {
        Retry.decorateCheckedRunnable(retry, invocation::invoke).run();

      } catch (Throwable e) {
        if (!errorPredicate.test(e)) throw e;

        fallback.invoke(invocation, e);
      }
    }
  }
}
