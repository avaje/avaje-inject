package io.avaje.inject.aop;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class InvocationFallbackTest {

  private final List<String> trace = new ArrayList<>();
  private final Method doStuffMethod;
  private final Object myArg = new Object();
  private boolean throwOnDoStuff;
  private Object usedArg;
  private Object fallbackArg;

  InvocationFallbackTest() {
    try {
      doStuffMethod = InvocationFallbackTest.class.getDeclaredMethod("doStuff", Object.class);
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }

  String doStuff(Object arg) {
    usedArg = arg;
    trace.add("doStuff");
    if (throwOnDoStuff) {
      throw new RuntimeException("nope");
    }
    return "hello";
  }

  public String fallbackDoStuff(Object arg) {
    fallbackArg = arg;
    trace.add("fallbackDoStuff");
    return "fallback-hello";
  }

  @Test
  void invokeWithFallback() throws Throwable {
    throwOnDoStuff = true;

    Invocation.Base<String> call =
        new Invocation.Call<>(() -> doStuff(myArg))
            .with(this, doStuffMethod, myArg)
            .fallback(e -> fallbackDoStuff(myArg));

    MyInterceptor myInterceptor = new MyInterceptor();

    // invoke the original method
    myInterceptor.invoke(call);

    assertThat(trace).containsExactly("doStuff", "fallbackDoStuff");
    assertThat(usedArg).isSameAs(myArg);
    assertThat(fallbackArg).isSameAs(myArg);
    assertThat(call.finalResult()).isEqualTo("fallback-hello");
  }

  @Test
  void invokeWithNoFallback() throws Throwable {
    throwOnDoStuff = false;

    Invocation.Base<String> call = new Invocation.Call<>(() -> doStuff(myArg))
      .with(this, doStuffMethod, myArg)
      .fallback(this::fallbackDoStuff);

     MyInterceptor myInterceptor = new MyInterceptor();

    // invoke the original method
    myInterceptor.invoke(call);

    assertThat(trace).containsExactly("doStuff");
    assertThat(usedArg).isSameAs(myArg);
    assertThat(fallbackArg).isNull();
    assertThat(call.finalResult()).isEqualTo("hello");
  }

  static class MyInterceptor implements MethodInterceptor {

    @Override
    public void invoke(Invocation invocation) throws Throwable {
      try {
        invocation.invoke();
      } catch (Throwable e) {
        invocation.invokeRecoveryMethod(e);
      }
    }
  }

}
