package org.example.myapp.lifecycle;

import io.avaje.inject.BeanScope;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PostConstructReturnsThisTest {

  @Test
  void beanWithPostConstructReturningSelf() {
    try (BeanScope beanScope = BeanScope.builder().build()) {
      var bean = beanScope.get(PostConstructReturnsThis.class);
      assertThat(bean).isNotNull();

      var self = bean.init();
      assertThat(bean).isSameAs(self);
    }
  }
}
