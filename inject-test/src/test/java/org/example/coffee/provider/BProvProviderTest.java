package org.example.coffee.provider;

import io.avaje.inject.BeanScope;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BProvProviderTest {

  @Test
  void providerOfGenericType() {
    try (BeanScope beanScope = BeanScope.builder().build()) {

      BProv<String> bProv = beanScope.get(BProvProvider$DI.TYPE_BProvString);
      assertThat(bProv.get()).isEqualTo("Hello BProv1");

      BProv<String> bProv2 = beanScope.get(BProvProvider$DI.TYPE_BProvString);
      assertThat(bProv).isNotSameAs(bProv2);
      assertThat(bProv2.get()).isEqualTo("Hello BProv2");
    }
  }

}
