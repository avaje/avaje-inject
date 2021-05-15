package org.example.coffee.provider;

import io.avaje.inject.ApplicationScope;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MethodInjectProviderTest {

  @Test
  void test() {

    MethodInjectProvider bean = ApplicationScope.get(MethodInjectProvider.class);
    AProv aProv = bean.testGet();

    assertThat(aProv).isNotNull();

    AProv beanDirect = ApplicationScope.get(AProv.class);
    assertThat(aProv).isSameAs(beanDirect);
  }

  @Test
  void emptyMethodInjection() {
    MethodInjectProvider bean = ApplicationScope.get(MethodInjectProvider.class);
    assertThat(bean.isEmptyMethodInjection()).isTrue();
  }
}
