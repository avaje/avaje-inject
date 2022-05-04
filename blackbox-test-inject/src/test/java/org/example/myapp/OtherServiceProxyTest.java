package org.example.myapp;

import io.avaje.inject.BeanScope;
import org.example.myapp.aspect.TraceAspect;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OtherServiceProxyTest {

  @Test
  void proxyMethodInvocation() {

    BeanScope beanScope = BeanScope.builder().build();
    OtherService otherService = beanScope.get(OtherService.class);

    String result = otherService.other("foo", 42);
    assertThat(result).isEqualTo("other foo 42");

    TraceAspect.clear();
    otherService.multi();

    List<String> trace = TraceAspect.obtain();
    assertThat(trace).containsExactly("MyTimedAspect-begin", "MyAroundAspect-begin", "MyAroundAspect-end", "MyTimedAspect-end");
  }
}
