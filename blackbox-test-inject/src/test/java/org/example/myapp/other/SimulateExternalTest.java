package org.example.myapp.other;

import io.avaje.inject.BeanScope;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SimulateExternalTest {

  @Test
  void doStuff() {
    try (BeanScope beanScope = BeanScope.builder().build()) {
      var simulateExternal = beanScope.get(SimulateExternal.class);
      var simulateExternal2 = beanScope.get(SimulateExternal2.class);
      var simulateExternalPub = beanScope.get(SimulateExternalPub.class);
      var simulateExternalPub2 = beanScope.get(SimulateExternalPub2.class);

      assertThat(simulateExternal).isNotNull();
      assertThat(simulateExternal2).isNotNull();
      assertThat(simulateExternalPub).isNotNull();
      assertThat(simulateExternalPub2).isNotNull();
    }
  }
}
