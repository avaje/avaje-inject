package org.example.myapp;

import io.avaje.inject.MethodInterceptor;
import io.avaje.inject.Invocation;
import org.example.myapp.aspect.MyAround;
import org.example.myapp.aspect.MyAroundAspect;
import org.example.myapp.aspect.MyBeforeAspect;

import java.lang.reflect.Method;

//@Proxy
//@Singleton
public class HelloServiceProxy extends HelloService {

  final MyBeforeAspect aspect;
  final MyAroundAspect aroundAspect;
  private final Method fooMethod;
  private final Method bazzMethod;
  private final MyAround myArround;
  private final MethodInterceptor bazzInterceptor;

  public HelloServiceProxy(HelloData data, MyBeforeAspect aspect, MyAroundAspect aroundAspect) {
    super(data);
    this.aspect = aspect;
    this.aroundAspect = aroundAspect;
    try {
      fooMethod = HelloService.class.getDeclaredMethod("foo", String.class);
      bazzMethod = HelloService.class.getDeclaredMethod("bazz", String.class, int.class);
      myArround = bazzMethod.getAnnotation(MyAround.class);


      bazzInterceptor = aroundAspect.interceptor(bazzMethod, myArround);

    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  @Override
  public String foo(String param) {
    //aspect.beforeInvoke(fooMethod);
    return super.foo(param);
  }

  @Override
  public String bazz(String param0, int param1) {
    var call = new Invocation.Call<>(() -> super.bazz(param0, param1))
      .arguments(param0, param1)
      .method(bazzMethod);
    try {
      bazzInterceptor.invoke(call);
      //aroundAspect.around3(call);
      return call.result();
    } catch (Throwable e) {
      throw new RuntimeException(e);
    }
  }

}
