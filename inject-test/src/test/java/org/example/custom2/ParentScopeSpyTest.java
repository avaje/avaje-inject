package org.example.custom2;

import io.avaje.inject.BeanScope;
import io.avaje.inject.spi.Builder;
import io.avaje.inject.spi.AvajeModule;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;

class ParentScopeSpyTest {

  @Test
  void parent() {
    try (var parent = BeanScope.builder()
      .modules(new MyTestModule())
      .build()) {

      var parentOne = parent.get(OcsOne.class);
      assertThat(parentOne.one()).isEqualTo("realOne");

      try (var child = BeanScope.builder()
        .parent(parent, false)
        .modules(new OtherModule())
        .build()) {

        OcsThree three = child.get(OcsThree.class);
        assertThat(three).isNotNull();
        var childOne = child.get(OcsOne.class);
        assertThat(childOne).isSameAs(parentOne);

        OcsTwo two = child.get(OcsTwo.class);
        assertThat(two.twoPlusOne()).isEqualTo("two+realOne");
      }
    }
  }

  @Test
  void parent_withSpy() {
    try (var parent = BeanScope.builder()
      .modules(new MyTestModule())
      .build()) {

      var parentOne = parent.get(OcsOne.class);
      assertThat(parentOne.one()).isEqualTo("realOne");

      try (var child = BeanScope.builder()
        .parent(parent, false)
        .modules(new OtherModule())
        .forTesting()
        .spy(OcsOne.class) // apply Spy to parent supplied bean
        .build()) {

        assertThat(child.get(OcsThree.class)).isNotNull();
        var childOne = child.get(OcsOne.class);
        assertThat(childOne).isNotSameAs(parentOne);

        OcsTwo two = child.get(OcsTwo.class);
        assertThat(two.twoPlusOne()).isEqualTo("two+realOne");

        // it's a spy
        Mockito.verify(childOne).one();
      }
    }
  }

  static class MyTestModule implements AvajeModule.Custom {

    private final Class<?>[] provides = new Class<?>[]{};
    private final Class<?>[] requires = new Class<?>[]{};
    private final Class<?>[] requiresPackages = new Class<?>[]{};
    private Builder builder;

    @Override
    public Class<?>[] provides() {
      return provides;
    }

    @Override
    public Class<?>[] requires() {
      return requires;
    }

    @Override
    public Class<?>[] requiresPackages() {
      return requiresPackages;
    }

    @Override
    public Class<?>[] classes() {
      return new Class<?>[]{
        org.example.custom2.OcsOne.class,
      };
    }

    @Override
    public void build(Builder builder) {
      this.builder = builder;
      build_custom2_OcsOne();
    }

    protected void build_custom2_OcsOne() {
      OcsOne$DI.build(builder);
    }

  }
}
