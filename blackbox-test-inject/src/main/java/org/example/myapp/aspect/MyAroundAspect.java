package org.example.myapp.aspect;

import io.avaje.inject.AspectProvider;
import io.avaje.inject.Invocation;
import io.avaje.inject.MethodInterceptor;
import jakarta.inject.Singleton;

import java.lang.reflect.Method;
import java.util.Arrays;

@Singleton
//@Aspect(target = MyAround.class)
public class MyAroundAspect implements AspectProvider<MyAround>, MethodInterceptor {

  @Override
  public MethodInterceptor interceptor(Method method, MyAround around) {
    return this;
  }

  @Override
  public void invoke(Invocation invoke) throws Throwable {
    System.out.println("before args: " + Arrays.toString(invoke.arguments()) + " method: " + invoke.method());
    try {
      invoke.invoke();
      invoke.invoke();
      invoke.invoke();
    } catch (Throwable e) {
      throw new RuntimeException(e);
    } finally {
      System.out.println("after");
    }
  }

}
