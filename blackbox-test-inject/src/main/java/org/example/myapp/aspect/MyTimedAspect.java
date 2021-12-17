package org.example.myapp.aspect;

import io.avaje.inject.AspectProvider;
import io.avaje.inject.Invocation;
import io.avaje.inject.MethodInterceptor;
import jakarta.inject.Singleton;

import java.lang.reflect.Method;

@Singleton
public class MyTimedAspect implements AspectProvider<MyTimed>, MethodInterceptor {

  @Override
  public MethodInterceptor interceptor(Method method, MyTimed aspectAnnotation) {
    return this;
  }

  @Override
  public void invoke(Invocation invocation) throws Throwable {
    TraceAspect.add("MyTimedAspect-begin");
    long start = System.nanoTime();
    try {
      invocation.invoke();
    } finally {
      long exeNanos = System.nanoTime() - start;
      String fullName = invocation.method().getName();
      System.out.println("executed " + fullName + " in " + exeNanos);
      TraceAspect.add("MyTimedAspect-end");
    }
  }
}
