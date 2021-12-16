package org.example.myapp.aspect;

import io.avaje.inject.AspectProvider;
import io.avaje.inject.Invocation;
import io.avaje.inject.MethodInterceptor;
import jakarta.inject.Singleton;

import java.lang.reflect.Method;

@Singleton
//@Aspect(target=MyBefore.class)
public class MyBeforeAspect implements AspectProvider<MyBefore> {

//  @Aspect.Before
//  public void beforeInvoke(Method method) {
//  }

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
