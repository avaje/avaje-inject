package org.example.coffee.qualifier;

import io.avaje.inject.xtra.ApplicationScope;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FooQualTest {

  @Test
  void test() {
    FooQual bean = ApplicationScope.get(FooQual.class);
    assertThat(bean).isNotNull();
  }
}
