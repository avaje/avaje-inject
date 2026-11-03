package org.example.myapp.injector;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.avaje.inject.BeanScope;
import io.avaje.inject.spi.GenericType;
import io.avaje.inject.spi.Injector;

class LegacyBaseAndIfaceInjectorTest {

  @Test
  void injectorFor_abstractClass() {
    try (BeanScope scope = BeanScope.builder().build()) {
      LegacyBaseImpl bean = new LegacyBaseImpl();
      assertThat(bean.dep1()).isNull();
      Injector<LegacyBase> injector = scope.get(new GenericType<Injector<LegacyBase>>() {});
      injector.inject(bean);
      assertThat(bean.dep1()).isNotNull();
    }
  }

  @Test
  void injectorFor_interface() {
    try (BeanScope scope = BeanScope.builder().build()) {
      LegacyIfaceImpl bean = new LegacyIfaceImpl();
      assertThat(bean.dep2()).isNull();

      Injector<LegacyIface> injector = scope.get(new GenericType<Injector<LegacyIface>>() {});
      injector.inject(bean);

      assertThat(bean.dep2()).isNotNull();
    }
  }
}
