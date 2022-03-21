package org.example.coffee.provider;

import io.avaje.inject.xtra.ApplicationScope;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FieldInjectProviderTest {

  @Test
  void test() {
    FieldInjectProvider bean = ApplicationScope.get(FieldInjectProvider.class);
    AProv aProv = bean.testGet();
    assertThat(aProv).isNotNull();

    AProv beanDirect = ApplicationScope.get(AProv.class);
    assertThat(aProv).isNotSameAs(beanDirect);
  }
}
