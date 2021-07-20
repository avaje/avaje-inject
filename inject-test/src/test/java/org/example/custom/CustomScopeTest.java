package org.example.custom;

import io.avaje.inject.BeanScope;
import org.example.coffee.CoffeeMaker;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CustomScopeTest {

  @Test
  void customScopeWithParent() {
    try (final BeanScope parentScope = BeanScope.newBuilder().build()) {
      final CoffeeMaker parentMaker = parentScope.get(CoffeeMaker.class);
      assertThat(parentMaker).isNotNull();

      LocalExt ext = new LocalExt();

      try (BeanScope beanScope = BeanScope.newBuilder()
        .withParent(parentScope)
        .withModules(new MyCustomModule())
        .withBeans(LocalExternal.class, ext)
        .build()) {

        final CoffeeMaker coffeeMaker = beanScope.get(CoffeeMaker.class);
        assertThat(coffeeMaker).isNotNull();

        final CustomBean bean = beanScope.get(CustomBean.class);
        final OtherCBean cBean = beanScope.get(OtherCBean.class);
        assertThat(bean).isNotNull();
        assertThat(cBean.dependency()).isSameAs(bean);

        final LocalExternal externallyProvided = beanScope.get(LocalExternal.class);
        assertThat(externallyProvided).isSameAs(ext);
      }
    }
  }

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

      final CoffeeMaker coffeeMaker = beanScope.get(CoffeeMaker.class);
      assertThat(coffeeMaker).isNull();
    }
  }

  static class LocalExt implements LocalExternal {

  }
}
