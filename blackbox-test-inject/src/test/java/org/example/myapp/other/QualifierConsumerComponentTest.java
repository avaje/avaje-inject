package org.example.myapp.other;

import io.avaje.inject.BeanScope;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class QualifierConsumerComponentTest {

  @Test
  void doStuff() {
    try (BeanScope beanScope = BeanScope.builder().build()) {
      var component = beanScope.get(QualifierConsumerComponent.class);

      assertThat(component.parent()).isEqualTo("parent-child");
      assertThat(component.child()).isEqualTo("child");
    }
  }
}
