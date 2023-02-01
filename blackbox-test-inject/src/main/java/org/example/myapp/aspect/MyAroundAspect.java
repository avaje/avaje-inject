package org.example.myapp.aspect;

import io.avaje.inject.aop.AspectProvider;
import io.avaje.inject.aop.Invocation;
import io.avaje.inject.aop.MethodInterceptor;
import jakarta.inject.Singleton;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Singleton
public class MyAroundAspect implements AspectProvider<MyAround>, MethodInterceptor {

  private final List<String> trace = new ArrayList<>();

  @Override
  public MethodInterceptor interceptor(Method method, MyAround around) {
    return this;
  }

  @Override
  public void invoke(Invocation invoke) throws Throwable {
    TraceAspect.add("MyAroundAspect-begin");
    trace.add(invoke.method().getName() + " args:" + Arrays.toString(invoke.arguments()));
    System.out.println("before args: " + Arrays.toString(invoke.arguments()) + " method: " + invoke.method());
    try {
      invoke.invoke();
      invoke.invoke();
      invoke.invoke();
    } finally {
      System.out.println("after");
      TraceAspect.add("MyAroundAspect-end");
    }
  }

  public List<String> trace() {
    ArrayList<String> copy = new ArrayList<>(trace);
    trace.clear();
    return copy;
  }
}
