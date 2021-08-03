package org.example.customext0;

import io.avaje.inject.BeanScope;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class Ext0ScopeTest {

  @Test
  void wire() {

    final BeanScope scope = BeanScope.newBuilder()
      .withBean(Ext0iface.class, new If0())
      .withBean(Ext0conc.class, new Ext0conc())
      .withModules(new Ext0Module())
      .build();

    final Ext0Other other = scope.get(Ext0Other.class);
    assertThat(other).isNotNull();
  }

  static class If0 implements Ext0iface {

  }
}
