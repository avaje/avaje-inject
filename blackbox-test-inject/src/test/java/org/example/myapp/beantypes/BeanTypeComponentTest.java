package org.example.myapp.beantypes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

import io.avaje.inject.BeanScope;

import java.io.Serializable;

class BeanTypesTest {

  @Test
  void testBeanTypesRestrictingInjection() {
    try (var scope = BeanScope.builder().build()) {

      assertFalse(scope.contains(BeanTypeComponent.class));
      assertThat(scope.get(AbstractSuperClass.class)).isNotNull();
      assertThat(scope.get(LimitedInterface.class)).isNotNull();
      assertThat(scope.get(Serializable.class)).isEqualTo("IAmSerializable");
      assertThat(scope.get(CharSequence.class)).isEqualTo("IAmNullable");
    }
  }
}
