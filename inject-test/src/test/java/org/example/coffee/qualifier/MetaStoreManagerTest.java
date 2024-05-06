package org.example.coffee.qualifier;

import io.avaje.inject.BeanScope;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MetaStoreManagerTest {

  @Test
  void test() {
    try (BeanScope context = BeanScope.builder().build()) {
      MegaStoreManager manager = context.get(MegaStoreManager.class);

      assertThat(manager.green).isInstanceOf(GreenStore.class);
      assertThat(manager.blue).isInstanceOf(BlueStore.class);
      assertThat(manager.noQualifier).isInstanceOf(NoNameStore.class);
    }
  }
}
