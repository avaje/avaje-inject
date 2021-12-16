package org.example.myapp;

import io.avaje.inject.Invocation;
import io.avaje.inject.MethodInterceptor;
import org.example.myapp.aspect.MyAround;
import org.example.myapp.aspect.MyAroundAspect;

import java.lang.reflect.Method;

//@Proxy
//@Singleton
public class OtherServiceProxy extends OtherService {

  final MyAroundAspect aroundAspect;

  private final Method otherMethod;
  private final MethodInterceptor otherInterceptor;

  public OtherServiceProxy(MyAroundAspect aroundAspect) {
    this.aroundAspect = aroundAspect;
    try {
      otherMethod = OtherService.class.getMethod("other", String.class, int.class);
      otherInterceptor = aroundAspect.interceptor(otherMethod, otherMethod.getAnnotation(MyAround.class));
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  @Override
  public String other(String param0, int param1) {
    var invocation = new Invocation.Call<>(() -> super.other(param0, param1)).method(otherMethod).arguments(param0, param1);
    try {
      otherInterceptor.invoke(invocation);
      return invocation.result();
    } catch (Throwable e) {
      throw new RuntimeException(e);
    }
  }

}
