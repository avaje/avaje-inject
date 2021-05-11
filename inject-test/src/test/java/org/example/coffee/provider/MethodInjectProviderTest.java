package org.example.coffee.provider;

import io.avaje.inject.SystemContext;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MethodInjectProviderTest {

  @Test
  void test() {

    MethodInjectProvider bean = SystemContext.getBean(MethodInjectProvider.class);
    AProv aProv = bean.testGet();

    assertThat(aProv).isNotNull();

    AProv beanDirect = SystemContext.getBean(AProv.class);
    assertThat(aProv).isSameAs(beanDirect);
  }

  @Test
  void emptyMethodInjection() {
    MethodInjectProvider bean = SystemContext.getBean(MethodInjectProvider.class);
    assertThat(bean.isEmptyMethodInjection()).isTrue();
  }
}
