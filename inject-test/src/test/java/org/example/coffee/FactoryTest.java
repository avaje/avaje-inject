package org.example.coffee;

import io.avaje.inject.BeanScope;
import org.example.coffee.factory.BFact;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FactoryTest {

  @Test
  void test() {
    try (BeanScope context = BeanScope.newBuilder().build()) {
      BFact bean = context.get(BFact.class);
      String b = bean.b();
      assertThat(b).isNotNull();
    }

  }
}
