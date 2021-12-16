package org.example.myapp.aspect;

import io.avaje.inject.Aspect;
import io.avaje.inject.Invocation;
import jakarta.inject.Singleton;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.util.function.Supplier;

@Singleton
//@Aspect(target = MyAround.class)
public class MyAroundAspect {

  @Aspect.Around
  public void around3(Invocation invoke) {
    System.out.println("before");
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
