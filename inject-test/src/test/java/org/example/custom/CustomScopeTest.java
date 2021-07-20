package org.example.custom;

import io.avaje.inject.BeanScope;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CustomScopeTest {

  @Test
  void buildSimpleCustomScope() {

    LocalExt ext = new LocalExt();

    try (BeanScope beanScope = BeanScope.newBuilder()
      .withModules(new MyCustomModule())
      .withBeans(LocalExternal.class, ext)
      .build()) {

      final CustomBean bean = beanScope.get(CustomBean.class);
      final OtherCBean cBean = beanScope.get(OtherCBean.class);
      assertThat(bean).isNotNull();
      assertThat(cBean.dependency()).isSameAs(bean);

      final LocalExternal externallyProvided = beanScope.get(LocalExternal.class);
      assertThat(externallyProvided).isSameAs(ext);
    }
  }

  static class LocalExt implements LocalExternal {

  }
}
