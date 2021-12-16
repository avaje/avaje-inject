package org.example.myapp;

import io.avaje.inject.Invocation;
import io.avaje.inject.spi.Proxy;
import org.example.myapp.aspect.MyTimed;
import org.example.myapp.aspect.MyTimedAspect;

import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.function.Supplier;

@Proxy
//@Singleton
public class ExampleServiceProxy extends ExampleService {

  final MyTimedAspect timedAspect;
  private final Method otherMethod;
  private final MyTimed otherMethodMyTimed;

  public ExampleServiceProxy(HelloService helloService, MyTimedAspect timedAspect) {
    super(helloService);
    this.timedAspect = timedAspect;
    try {
      otherMethod = ExampleService.class.getDeclaredMethod("other", String.class, int.class);
      otherMethodMyTimed = otherMethod.getAnnotation(MyTimed.class);

    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  @Override
  public String other(String param0, int param1) {// throws IOException, IllegalStateException {
    try {
      final var invocation = new Invocation.CheckedCall<>(() -> super.other(param0, param1));
      timedAspect.around3(otherMethod, invocation, param0, param1);
      return invocation.result();
    //} catch (IOException | IllegalStateException e) {
    //  throw e;
    } catch (Throwable e) {
      throw new IllegalStateException(e);
    }
  }


  @Override
  public void runOnly(String param) {
    try {
      final var invocation = new Invocation.Run(() -> super.runOnly(param));
      timedAspect.around3(otherMethod, invocation, param);
    } catch (Throwable e) {
      throw new IllegalStateException(e);
    }
  }

}
