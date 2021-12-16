package org.example.myapp;

import io.avaje.inject.Invocation;
import io.avaje.inject.spi.Proxy;
import jakarta.inject.Singleton;
import org.example.myapp.aspect.MyAround;
import org.example.myapp.aspect.MyAroundAspect;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;

@Proxy
//@Singleton
public class OtherServiceProxy extends OtherService {

  final MyAroundAspect aroundAspect;
  private final Method otherMethod;
  private final MyAround myAround;

  public OtherServiceProxy(MyAroundAspect aroundAspect) {
    this.aroundAspect = aroundAspect;
    try {
      otherMethod = OtherService.class.getMethod("other", String.class, int.class);
      myAround = otherMethod.getAnnotation(MyAround.class);
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  @Override
  public String other(String param0, int param1) {
    var invocation = new Invocation.Call<>(() -> super.other(param0, param1));
    aroundAspect.around3(invocation);
    return invocation.result();
  }

}
