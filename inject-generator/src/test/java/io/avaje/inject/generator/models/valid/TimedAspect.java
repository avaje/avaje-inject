package io.avaje.inject.generator.models.valid;

import io.avaje.inject.aop.AspectProvider;
import io.avaje.inject.aop.Invocation;
import io.avaje.inject.aop.MethodInterceptor;
import jakarta.inject.Singleton;
import java.lang.reflect.Method;

@Singleton
public class TimedAspect implements AspectProvider<Timed>, MethodInterceptor {

  @Override
  public MethodInterceptor interceptor(Method method, Timed aspectAnnotation) {
    return this;
  }

  @Override
  public void invoke(Invocation invocation) throws Throwable {

    long start = System.nanoTime();
    try {
      invocation.invoke();
    } finally {
      long exeNanos = System.nanoTime() - start;
      String fullName = invocation.method().getName();
      System.out.println("executed " + fullName + " in " + exeNanos);
    }
  }
}
