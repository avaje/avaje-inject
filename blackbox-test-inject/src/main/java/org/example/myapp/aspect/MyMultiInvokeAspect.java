package org.example.myapp.aspect;

import io.avaje.inject.AspectProvider;
import io.avaje.inject.Invocation;
import io.avaje.inject.MethodInterceptor;
import jakarta.inject.Singleton;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class MyMultiInvokeAspect implements AspectProvider<MyMultiInvoke>, MethodInterceptor {

  public List<Object> results = new ArrayList<>();

  @Override
  public MethodInterceptor interceptor(Method method, MyMultiInvoke aspectAnnotation) {
    return this;
  }

  @Override
  public void invoke(Invocation invocation) throws Throwable {
    for (int i = 0; i < 5; i++) {
      results.add(invocation.invoke());
    }
  }

}
