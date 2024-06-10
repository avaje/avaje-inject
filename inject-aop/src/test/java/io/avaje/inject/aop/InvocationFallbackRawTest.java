package io.avaje.inject.aop;

import org.junit.jupiter.api.Test;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class InvocationFallbackRawTest {

  private final MethodHandles.Lookup lookup = MethodHandles.lookup();

  private final List<String> trace = new ArrayList<>();
  private final List<Method> methods = new ArrayList<>();
  private final List<Object> results = new ArrayList<>();
  private final List<Object> args = new ArrayList<>();

  private final Method doStuffMethod;
  private final Object myArg = new Object();
  private Object usedArg;
  private Object fallbackArg;

  InvocationFallbackRawTest() {
    try {
      doStuffMethod = InvocationFallbackRawTest.class.getDeclaredMethod("doStuff", Object.class);
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }

  String doStuff(Object arg) {
    usedArg = arg;
    trace.add("doStuff");
    return "hello";
  }

  String fallbackDoStuff(Object arg) {
    fallbackArg = arg;
    trace.add("fallbackDoStuff");
    return "fallback-hello";
  }

  @Test
  void invokeFallback() throws Throwable {
    Invocation.Base<String> call = new Invocation.Call<>(() -> this.doStuff(myArg))
      .with(this, doStuffMethod, myArg);

    MyInterceptor myInterceptor = new MyInterceptor();

    // fallback via reflection Method
    Method fallbackMethod = InvocationFallbackRawTest.class.getDeclaredMethod("fallbackDoStuff", Object.class);
    myInterceptor.invokeFallback(call, fallbackMethod);

    assertThat(fallbackArg).isSameAs(myArg);
    assertThat(trace).containsExactly("fallbackDoStuff");

    fallbackArg = null;
    // fallback via MethodHandle
    MethodHandle handle = lookup.findVirtual(InvocationFallbackRawTest.class, "fallbackDoStuff", MethodType.methodType(String.class, Object.class));
    myInterceptor.invokeFallbackViaHandle(call, handle);

    assertThat(fallbackArg).isSameAs(myArg);
    assertThat(trace).containsExactly("fallbackDoStuff", "fallbackDoStuff");
  }

  static class MyInterceptor {

    void invokeFallback(Invocation call, Method fallbackMethod) throws InvocationTargetException, IllegalAccessException {
      fallbackMethod.invoke(call.instance(), call.arguments());
    }

    void invokeFallbackViaHandle(Invocation call, MethodHandle handle) throws Throwable {
      Object[] args = call.arguments();
      switch (args.length) {
        case 1:
          call.result(handle.invoke(call.instance(), args[0]));
          break;
        case 2:
          call.result(handle.invoke(call.instance(), args[0], args[1]));
          break;

      }

    }
  }

}
