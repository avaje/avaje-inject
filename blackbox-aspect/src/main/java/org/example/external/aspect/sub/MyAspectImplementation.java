package org.example.external.aspect.sub;

import io.avaje.inject.Component;
import io.avaje.inject.aop.AspectProvider;
import io.avaje.inject.aop.Invocation;
import io.avaje.inject.aop.MethodInterceptor;
import org.example.external.aspect.MyExternalAspect;

import java.lang.reflect.Method;
import java.util.Arrays;

@Component
public class MyAspectImplementation implements AspectProvider<MyExternalAspect>, MethodInterceptor {

  @Override
  public MethodInterceptor interceptor(Method method, MyExternalAspect aspectAnnotation) {
    return this;
  }

  @Override
  public void invoke(Invocation invoke) throws Throwable {
    System.out.println("before args: " + Arrays.toString(invoke.arguments()) + " method: " + invoke.method());
    try {
      invoke.invoke();
    } finally {
      System.out.println("after method: " + invoke.method());
    }
  }
}
