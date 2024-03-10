package org.example.coffee.provider;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import io.avaje.inject.BeanScope;
import io.avaje.inject.spi.GenericType;

class FactoryProviderTest {

  @Test
  void test() {
    var scope = BeanScope.builder().build();

    Supplier<String> prov = BeanScope.builder().build().get(new GenericType<Supplier<String>>() {});
    assertThat(prov.get()).isEqualTo("Stand proud Provider, you were strong");
    assertThat(scope.get(String.class, "second")).isEqualTo("Nah, I'd win");
  }
}
