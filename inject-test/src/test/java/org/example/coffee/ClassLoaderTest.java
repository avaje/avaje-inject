package org.example.coffee;

import io.avaje.inject.BeanScope;
import org.example.coffee.provider.ProvOther;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ClassLoaderTest {

  @Test
  void testUsingClassLoader() {
    try (BeanScope context = BeanScope.builder()
      .classLoader(Thread.currentThread().getContextClassLoader())
      .build()) {
      ProvOther bean = context.get(ProvOther.class);
      String other = bean.other();
      assertThat(other).isEqualTo("mush mush beans");
    }
  }
}
