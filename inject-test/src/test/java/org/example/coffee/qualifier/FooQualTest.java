package org.example.coffee.qualifier;

import io.avaje.inject.SystemContext;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FooQualTest {

  @Test
  void test() {
    FooQual bean = SystemContext.getBean(FooQual.class);
    assertThat(bean).isNotNull();
  }
}
