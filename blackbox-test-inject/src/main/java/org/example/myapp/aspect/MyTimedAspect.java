package org.example.myapp.aspect;

import io.avaje.inject.Aspect;
import io.avaje.inject.Invocation;
import jakarta.inject.Singleton;

import java.lang.reflect.Method;

@Singleton
//@Aspect(target = MyAround.class)
public class MyTimedAspect {

  @Aspect.Around
  public void around3(Method method, Invocation invocation, Object... args) throws Throwable {
    long start = System.nanoTime();
    try {
      invocation.invoke();
    } finally {
      long exeNanos = System.nanoTime() - start;
      String fullName = method.getName();
      System.out.println("executed " + fullName + " in " + exeNanos);
    }
  }
}
