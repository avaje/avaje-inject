package org.example.myapp.aspect;

import io.avaje.inject.aop.AspectProvider;
import io.avaje.inject.aop.Invocation;
import io.avaje.inject.aop.MethodInterceptor;
import javax.inject.Singleton;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class MyThrowingAspect implements AspectProvider<MyThrowing>, MethodInterceptor {

  @Override
  public MethodInterceptor interceptor(Method method, MyThrowing around) {
    return this;
  }

  @Override
  public void invoke(Invocation invoke) throws Throwable {
    throw new ArithmeticException("my interceptor throws this");
  }
}
