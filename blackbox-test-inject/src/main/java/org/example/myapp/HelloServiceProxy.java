package org.example.myapp;

import io.avaje.inject.spi.Proxy;
import io.avaje.inject.Invocation;
import org.example.myapp.aspect.MyAround;
import org.example.myapp.aspect.MyAroundAspect;
import org.example.myapp.aspect.MyBeforeAspect;

import java.lang.reflect.Method;

@Proxy
//@Singleton
public class HelloServiceProxy extends HelloService {

  final MyBeforeAspect aspect;
  final MyAroundAspect aroundAspect;
  private final Method fooMethod;
  private final Method bazzMethod;
  private final MyAround myArround;

  public HelloServiceProxy(HelloData data, MyBeforeAspect aspect, MyAroundAspect aroundAspect) {
    super(data);
    this.aspect = aspect;
    this.aroundAspect = aroundAspect;
    try {
      fooMethod = HelloService.class.getDeclaredMethod("foo", String.class);
      bazzMethod = HelloService.class.getDeclaredMethod("bazz", String.class, int.class);
      myArround = bazzMethod.getAnnotation(MyAround.class);
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  @Override
  public String foo(String param) {
    aspect.beforeInvoke(fooMethod);
    return super.foo(param);
  }

  @Override
  public String bazz(String param0, int param1) {
    var call = new Invocation.Call<>(() -> super.bazz(param0, param1));
    aroundAspect.around3(call);
    return call.result();
  }

}
