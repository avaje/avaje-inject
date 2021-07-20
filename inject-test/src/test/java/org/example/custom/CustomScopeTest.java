package org.example.custom;

import io.avaje.inject.BeanScope;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CustomScopeTest {

  @Test
  void buildSimpleCustomScope() {
    try (BeanScope beanScope = BeanScope.newBuilder()
      .withModules(new MyCustomModule())
      .build()) {

      final CustomBean bean = beanScope.get(CustomBean.class);
      final OtherCBean cBean = beanScope.get(OtherCBean.class);
      assertThat(bean).isNotNull();
      assertThat(cBean.dependency()).isSameAs(bean);
    }
  }

}
