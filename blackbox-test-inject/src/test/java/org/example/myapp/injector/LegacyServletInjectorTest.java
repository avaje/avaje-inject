package org.example.myapp.injector;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.avaje.inject.BeanScope;
import io.avaje.inject.spi.GenericType;
import io.avaje.inject.spi.Injector;

class LegacyServletInjectorTest {

  @Test
  void generatedInjector_wiresExternallyCreatedInstance() {
    try (BeanScope scope = BeanScope.builder().build()) {
      LegacyServlet servlet = new LegacyServlet();
      assertThat(servlet.dep1()).isNull();
      assertThat(servlet.dep2()).isNull();

      Injector<LegacyServlet> injector = scope.get(new GenericType<Injector<LegacyServlet>>() {});
      injector.inject(servlet);

      assertThat(servlet.dep1()).isNotNull();
      assertThat(servlet.dep2()).isNotNull();
    }
  }

  @Test
  void scopeClose_preDestroysAllInjectedInstances() {
    LegacyServlet servlet1 = new LegacyServlet();
    LegacyServlet servlet2 = new LegacyServlet();

    try (BeanScope scope = BeanScope.builder().build()) {
      Injector<LegacyServlet> injector = scope.get(new GenericType<Injector<LegacyServlet>>() {});
      injector.inject(servlet1);
      injector.inject(servlet2);

      assertThat(servlet1.destroyed()).isFalse();
      assertThat(servlet2.destroyed()).isFalse();
    }

    assertThat(servlet1.destroyed()).isTrue();
    assertThat(servlet2.destroyed()).isTrue();
  }
}
