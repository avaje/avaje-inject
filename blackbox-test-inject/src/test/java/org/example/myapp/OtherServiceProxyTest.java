package org.example.myapp;

import io.avaje.inject.BeanScope;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OtherServiceProxyTest {

  @Test
  void proxyMethodInvocation() {

    BeanScope beanScope = BeanScope.newBuilder().build();
    OtherService otherService = beanScope.get(OtherService.class);

    String result = otherService.other("foo", 42);

    assertThat(result).isEqualTo("other foo 42");

    //OtherServiceProxy proxy = beanScope.get(OtherServiceProxy.class);
    //proxy.other3("asd", 23);
  }
}
