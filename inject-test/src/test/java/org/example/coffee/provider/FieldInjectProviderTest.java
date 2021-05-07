package org.example.coffee.provider;

import io.avaje.inject.SystemContext;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FieldInjectProviderTest {

  @Test
  void test() {

    FieldInjectProvider bean = SystemContext.getBean(FieldInjectProvider.class);
    AProv aProv = bean.testGet();

    assertThat(aProv).isNotNull();

    AProv beanDirect = SystemContext.getBean(AProv.class);
    assertThat(aProv).isSameAs(beanDirect);
  }
}
