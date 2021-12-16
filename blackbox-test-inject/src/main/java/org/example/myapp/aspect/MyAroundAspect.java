package org.example.myapp.aspect;

import io.avaje.inject.AspectProvider;
import io.avaje.inject.Invocation;
import io.avaje.inject.MethodInterceptor;
import jakarta.inject.Singleton;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Singleton
//@Aspect(target = MyAround.class)
public class MyAroundAspect implements AspectProvider<MyAround>, MethodInterceptor {

  private List<String> trace = new ArrayList<>();

  @Override
  public MethodInterceptor interceptor(Method method, MyAround around) {
    return this;
  }

  @Override
  public void invoke(Invocation invoke) throws Throwable {
    trace.add(invoke.method().getName() + " args:" + Arrays.toString(invoke.arguments()));
    System.out.println("before args: " + Arrays.toString(invoke.arguments()) + " method: " + invoke.method());
    try {
      invoke.invoke();
      invoke.invoke();
      invoke.invoke();
    } finally {
      System.out.println("after");
    }
  }

  public List<String> trace() {
    ArrayList<String> result = new ArrayList<>(trace);
    trace.clear();
    return result;
  }
}