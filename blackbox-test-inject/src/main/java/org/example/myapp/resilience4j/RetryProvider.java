package org.example.myapp.resilience4j;

import java.lang.reflect.Method;
import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.concurrent.atomic.AtomicInteger;

import io.avaje.inject.Component;
import io.avaje.inject.aop.AspectProvider;
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

    return new RetryFallbackInterceptor(retry);
  }

  public static AtomicInteger testCounter = new AtomicInteger();

  static class RetryFallbackInterceptor implements MethodInterceptor {

    private final io.github.resilience4j.retry.Retry retry;

    public RetryFallbackInterceptor(Retry retry) {
      this.retry = retry;
    }

    @Override
    public void invoke(Invocation invocation) throws Throwable {
      testCounter.incrementAndGet();
      if (invocation.hasRecoveryMethod()) {

        Retry.decorateCheckedSupplier(retry, invocation::invoke)
            .recover(throwable -> () -> invocation.invokeRecoveryMethod(throwable))
            .apply();
      } else {
        Retry.decorateCheckedSupplier(retry, invocation::invoke).apply();
      }
    }
  }
}
