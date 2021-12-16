package org.example.myapp;

import io.avaje.inject.Invocation;
import io.avaje.inject.MethodInterceptor;
import io.avaje.inject.spi.Proxy;
import org.example.myapp.aspect.MyTimed;
import org.example.myapp.aspect.MyTimedAspect;

import java.lang.reflect.Method;

@Proxy
//@Singleton
public class ExampleServiceProxy extends ExampleService {

  final MyTimedAspect timedAspect;

  private final Method otherMethod;
  private final MethodInterceptor otherInterceptor;
  private final Method runOnlyMethod;
  private final MethodInterceptor runOnlyInterceptor;

  public ExampleServiceProxy(HelloService helloService, MyTimedAspect timedAspect) {
    super(helloService);
    this.timedAspect = timedAspect;
    try {
      otherMethod = _findMethodFor("other", String.class, int.class);
      otherInterceptor = timedAspect.interceptor(otherMethod, otherMethod.getAnnotation(MyTimed.class));

      runOnlyMethod = _findMethodFor("runOnly", String.class);
      runOnlyInterceptor = timedAspect.interceptor(runOnlyMethod, runOnlyMethod.getAnnotation(MyTimed.class));

    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  private Method _findMethodFor(String name, Class<?>... types) throws NoSuchMethodException {
    return ExampleService.class.getDeclaredMethod(name, types);
  }

  @Override
  public String other(String param0, int param1) {// throws IOException, IllegalStateException {
    try {
      final var invocation = new Invocation.CheckedCall<>(() -> super.other(param0, param1)).method(otherMethod);
      otherInterceptor.invoke(invocation);
      return invocation.result();
      //} catch (IOException | IllegalStateException e) {
      //  throw e;
    } catch (RuntimeException e) {
      throw e;
    } catch (Throwable e) {
      throw new IllegalStateException(e);
    }
  }


  @Override
  public void runOnly(String param) {
    try {
      final var invocation = new Invocation.Run(() -> super.runOnly(param)).method(runOnlyMethod);
      runOnlyInterceptor.invoke(invocation);
    } catch (Throwable e) {
      throw new IllegalStateException(e);
    }
  }

}
