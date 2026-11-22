package org.example.myapp.injector;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.NoSuchElementException;

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
  void beanScopeInject() {
    try (BeanScope scope = BeanScope.builder().build()) {
      LegacyServlet servlet = scope.inject(new LegacyServlet());
      assertThat(servlet.dep1()).isNotNull();
      assertThat(servlet.dep2()).isNotNull();
    }
  }

  @Test
  void beanScopeInject_noInjector_throws() {
    try (BeanScope scope = BeanScope.builder().build()) {
      assertThatThrownBy(() -> scope.inject(new Object()))
          .isInstanceOf(NoSuchElementException.class)
          .hasMessageContaining("java.lang.Object");
    }
  }
}
