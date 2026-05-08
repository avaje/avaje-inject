package org.example.myapp.lifecycle;

import io.avaje.inject.BeanScope;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ThrowingLifecycleTest {

  @Test
  void beanWithPostConstruct_withCheckedExceptions() {
    try (BeanScope beanScope = BeanScope.builder().build()) {
      var bean = beanScope.get(ThrowingLifecycle.class);
      assertThat(bean).isNotNull();
      assertThat(bean.started).isTrue();

      var bean2 = beanScope.get(ThrowingLifecycleWithScope.class);
      assertThat(bean2).isNotNull();
      assertThat(bean2.started).isTrue();
    }
  }
}
