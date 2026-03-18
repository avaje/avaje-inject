package org.example.myapp.assist.generic;

import io.avaje.inject.BeanScope;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RelicTest {

  @Test
  void methodTypeParamFactory() {
    try (BeanScope scope = BeanScope.builder().build()) {
      RelicFactory factory = scope.get(RelicFactory.class);

      Relic<String> stringRelic = factory.forge(String.class);
      assertThat(stringRelic.type()).isEqualTo(String.class);
      assertThat(stringRelic.somthin()).isNotNull();

      Relic<Integer> intRelic = factory.forge(Integer.class);
      assertThat(intRelic.type()).isEqualTo(Integer.class);
      assertThat(intRelic.somthin()).isNotNull();
    }
  }
}
