package org.example.customext0;

import io.avaje.inject.BeanScope;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class Ext0ScopeTest {

  @Test
  void wire() {

    final BeanScope scope = BeanScope.newBuilder()
      .withModules(new Ext0Module(new If0(), new Ext0conc()))
      .build();

    final Ext0Other other = scope.get(Ext0Other.class);
    assertThat(other).isNotNull();
  }

  static class If0 implements Ext0iface {

  }
}
