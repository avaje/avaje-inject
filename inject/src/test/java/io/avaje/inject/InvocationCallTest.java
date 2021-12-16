package io.avaje.inject;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class InvocationCallTest {

  private final List<String> trace = new ArrayList<>();
  private final List<Method> methods = new ArrayList<>();
  private final List<Object> results = new ArrayList<>();
  private final List<Object> args = new ArrayList<>();

  private final Method doStuffMethod;
  private final Object myArg = new Object();
  private Object usedArg;

  InvocationCallTest() {
    try {
      doStuffMethod = InvocationCallTest.class.getDeclaredMethod("doStuff", Object.class);
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }

  String doStuff(Object arg) {
    usedArg = arg;
    trace.add("doStuff");
    return "hello";
  }

  @Test
  void single() throws Throwable {
    Invocation.Build<String> call = new Invocation.Call<>(() -> this.doStuff(myArg))
      .arguments(myArg)
      .method(doStuffMethod);

    new Inter0().invoke(call);

    assertThat(trace).containsExactly("b-Inter0", "doStuff", "a-Inter0");
    assertThat(usedArg).isSameAs(myArg);
    assertThat(methods).containsExactly(doStuffMethod);
    assertThat(results).containsExactly("hello");
    assertThat(args).containsExactly(myArg);
  }

  @Test
  void wrapped() throws Throwable {
    Invocation.Build<String> call = new Invocation.Call<>(() -> this.doStuff(myArg))
      .arguments(myArg)
      .method(doStuffMethod)
      .wrap(new Inter0());

    new Inter1().invoke(call);

    assertThat(trace).containsExactly("b-Inter1", "b-Inter0", "doStuff", "a-Inter0", "a-Inter1");
    assertThat(usedArg).isSameAs(myArg);
    assertThat(methods).containsExactly(doStuffMethod, doStuffMethod);
    assertThat(results).containsExactly("hello", "hello");
    assertThat(args).containsExactly(myArg, myArg);
  }

  @Test
  void wrapped_wrapped() throws Throwable {
    Invocation.Build<String> call = new Invocation.Call<>(() -> this.doStuff(myArg))
      .arguments(myArg)
      .method(doStuffMethod)
      .wrap(new Inter0())
      .wrap(new Inter1());

    new Inter2().invoke(call);

    assertThat(trace).containsExactly("b-Inter2", "b-Inter1", "b-Inter0", "doStuff", "a-Inter0", "a-Inter1", "a-Inter2");
    assertThat(usedArg).isSameAs(myArg);
    assertThat(methods).containsExactly(doStuffMethod, doStuffMethod, doStuffMethod);
    assertThat(results).containsExactly("hello", "hello", "hello");
    assertThat(args).containsExactly(myArg, myArg, myArg);
  }

  class Inter0 implements MethodInterceptor {

    @Override
    public void invoke(Invocation invocation) throws Throwable {
      args.addAll(Arrays.asList(invocation.arguments()));
      methods.add(invocation.method());
      trace.add("b-Inter0");
      results.add(invocation.invoke());
      trace.add("a-Inter0");
    }
  }

  class Inter1 implements MethodInterceptor {

    @Override
    public void invoke(Invocation invocation) throws Throwable {
      args.addAll(Arrays.asList(invocation.arguments()));
      methods.add(invocation.method());
      trace.add("b-Inter1");
      results.add(invocation.invoke());
      trace.add("a-Inter1");
    }
  }

  class Inter2 implements MethodInterceptor {

    @Override
    public void invoke(Invocation invocation) throws Throwable {
      args.addAll(Arrays.asList(invocation.arguments()));
      methods.add(invocation.method());
      trace.add("b-Inter2");
      results.add(invocation.invoke());
      trace.add("a-Inter2");
    }
  }
}
