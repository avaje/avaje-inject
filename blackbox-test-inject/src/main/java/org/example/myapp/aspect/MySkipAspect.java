package org.example.myapp.aspect;

import io.avaje.inject.AspectProvider;
import io.avaje.inject.Invocation;
import io.avaje.inject.MethodInterceptor;
import jakarta.inject.Singleton;

import java.lang.reflect.Method;

@Singleton
public class MySkipAspect implements AspectProvider<MySkip>, MethodInterceptor {

  @Override
  public MethodInterceptor interceptor(Method method, MySkip aspectAnnotation) {
    return this;
  }
  @Override
  public void invoke(Invocation invocation) throws Throwable {
    // just over-write the result, never invoke underlying method
    invocation.result("my-skip-result");
  }

}
