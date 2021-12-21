package org.example.myapp.aspect;

import io.avaje.inject.aop.AspectProvider;
import io.avaje.inject.aop.Invocation;
import io.avaje.inject.aop.MethodInterceptor;
import javax.inject.Singleton;

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
