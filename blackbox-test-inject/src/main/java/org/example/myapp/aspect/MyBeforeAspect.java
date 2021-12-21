package org.example.myapp.aspect;

import io.avaje.inject.aop.AspectProvider;
import io.avaje.inject.aop.Invocation;
import io.avaje.inject.aop.MethodInterceptor;
import javax.inject.Singleton;

import java.lang.reflect.Method;

@Singleton
public class MyBeforeAspect implements AspectProvider<MyBefore> {

  @Override
  public MethodInterceptor interceptor(Method method, MyBefore aspectAnnotation) {
    return new Intercept(method);
  }

  static final class Intercept implements MethodInterceptor {
    final Method method;

    Intercept(Method method) {
      this.method = method;
    }

    @Override
    public void invoke(Invocation invocation) throws Throwable {
      if (method.getName().equals("foo")) {
        throw new IllegalStateException("Can't call this method");
      }
    }
  }

}
