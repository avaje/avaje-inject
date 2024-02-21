package org.example.custom;

import io.avaje.inject.BeanEntry;
import io.avaje.inject.BeanScope;
import org.example.MyCustomScope;
import org.example.coffee.CoffeeMaker;
import org.example.custom.loc.LocalExternal;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

class CustomScopeTest {

  @Test
  void customScopeWithParent() {
    try (final BeanScope parentScope = BeanScope.builder().build()) {
      final CoffeeMaker parentMaker = parentScope.get(CoffeeMaker.class);
      assertThat(parentMaker).isNotNull();

      LocalExt ext = new LocalExt();

      try (BeanScope beanScope = BeanScope.builder()
        .parent(parentScope)
        .modules(new MyCustomModule(ext))
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
    CoffeeMaker suppliedCoffeeMaker = Mockito.mock(CoffeeMaker.class);

    try (BeanScope beanScope = BeanScope.builder()
      .modules(new MyCustomModule(ext))
      .bean(CoffeeMaker.class, suppliedCoffeeMaker)
      .build()) {

      final CustomBean bean = beanScope.get(CustomBean.class);
      final OtherCBean cBean = beanScope.get(OtherCBean.class);
      assertThat(bean).isNotNull();
      assertThat(cBean.dependency()).isSameAs(bean);

      final LocalExternal externallyProvided = beanScope.get(LocalExternal.class);
      assertThat(externallyProvided).isSameAs(ext);

      final CoffeeMaker coffeeMaker = beanScope.get(CoffeeMaker.class);
      assertThat(coffeeMaker).isSameAs(suppliedCoffeeMaker);
    }
  }

  @Test
  void customScopeAll() {

    LocalExt ext = new LocalExt();
    CoffeeMaker suppliedCoffeeMaker = Mockito.mock(CoffeeMaker.class);

    try (BeanScope beanScope = BeanScope.builder()
      .modules(new MyCustomModule(ext))
      .bean(CoffeeMaker.class, suppliedCoffeeMaker)
      .build()) {

      // includes the 2 supplied beans
      final List<BeanEntry> all = beanScope.all();
      assertThat(all).hasSize(7);

      final CustomBean customBean = beanScope.get(CustomBean.class);

      final Optional<BeanEntry> customBeanEntry = all.stream()
        .filter(beanEntry -> beanEntry.hasKey(CustomBean.class))
        .findFirst();

      assertThat(customBeanEntry).isPresent();
      assertThat(customBeanEntry.get().bean()).isSameAs(customBean);
      assertThat(customBeanEntry.get().qualifierName()).isEqualTo("hello");
      assertThat(customBeanEntry.get().priority()).isEqualTo(0);
      assertThat(customBeanEntry.get().keys()).containsExactly(CustomBean.class.getCanonicalName());


      final Optional<BeanEntry> fooCustomEntry = all.stream()
        .filter(beanEntry -> beanEntry.hasKey(FooCustom.class))
        .findFirst();

      assertThat(fooCustomEntry).isPresent();
      assertThat(fooCustomEntry.get().qualifierName()).isNull();

      // only the beans with MyCustomScope annotation
      final List<BeanEntry> myCustomScopeBeans = all.stream()
        .filter(beanEntry -> beanEntry.type().isAnnotationPresent(MyCustomScope.class))
        .collect(toList());
      assertThat(myCustomScopeBeans).hasSize(3);

      final OtherCBean cBean = beanScope.get(OtherCBean.class);
      assertThat(customBean).isNotNull();
      assertThat(cBean.dependency()).isSameAs(customBean);

      final LocalExternal externallyProvided = beanScope.get(LocalExternal.class);
      assertThat(externallyProvided).isSameAs(ext);

      final CoffeeMaker coffeeMaker = beanScope.get(CoffeeMaker.class);
      assertThat(coffeeMaker).isSameAs(suppliedCoffeeMaker);
    }
  }

  static class LocalExt implements LocalExternal {

  }
}
