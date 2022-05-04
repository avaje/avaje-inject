package org.example.customext1;

import io.avaje.inject.BeanScope;
import org.example.customext0.Ext0Other;
import org.example.customext0.Ext0conc;
import org.example.customext0.Ext0iface;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class Ext1ScopeTest {

/*
  Unfortunately with both modules in test-classes plus this test it can not compile.
  So this wireParentChild() works when scopes are in src/main (and the test here in src/test).

  @Test
  void wireParentChild() {

    final BeanScope parentScope = BeanScope.builder()
      .bean(Ext0iface.class, new If0())
      .bean(Ext0conc.class, new Ext0conc())
      .modules(new Ext0Module())
      .build();

    final BeanScope scope = BeanScope.builder()
      .bean(Ext1iface.class, new If1())
      .bean(Ext1conc.class, new Ext1conc())
      .modules(new Ext1Module())
      .parent(parentScope)
      .build();

    final Ext1Bean ext1Bean = scope.get(Ext1Bean.class);
    assertThat(ext1Bean).isNotNull();

    assertNotNull(ext1Bean.ext0iface());
    assertNotNull(ext1Bean.ext0conc());
    assertNotNull(ext1Bean.ext0Other());
    assertNotNull(ext1Bean.ext1iface());
    assertNotNull(ext1Bean.ext1conc());
  }
*/

  @Test
  void wireBoth() {

    // wire everything using only Ext1Module so simulating
    // Ext0Module via external dependencies
    final BeanScope scope = BeanScope.builder()
      .bean(Ext0iface.class, new If0())
      .bean(Ext0conc.class, new Ext0conc())
      .bean(Ext0Other.class, new Ext0Other())
      .modules(new Ext1Module(new If1(), new Ext1conc())) //new Ext0Module(),
      .build();

    final Ext1Bean ext1Bean = scope.get(Ext1Bean.class);
    assertThat(ext1Bean).isNotNull();

    assertNotNull(ext1Bean.ext0iface());
    assertNotNull(ext1Bean.ext0conc());
    assertNotNull(ext1Bean.ext0Other());
    assertNotNull(ext1Bean.ext1iface());
    assertNotNull(ext1Bean.ext1conc());
  }

  static class If0 implements Ext0iface {

  }
  static class If1 implements Ext1iface {

  }
}
