package org.example.autonamed;

import io.avaje.inject.BeanScope;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MyAutoNameTest {

  @Test
  void impliedNames_multiple() {

    try (final BeanScope beanScope = BeanScope.builder().build()) {

      final MyAutoB2 myAutoName = beanScope.get(MyAutoB2.class);
      assertThat(myAutoName.one()).isEqualTo("oneB2");
      assertThat(myAutoName.two()).isEqualTo("twoB2");

      final MyAutoB2Explicit myAutoB2Explicit = beanScope.get(MyAutoB2Explicit.class);
      assertThat(myAutoB2Explicit.one()).isEqualTo("oneB2");
      assertThat(myAutoB2Explicit.two()).isEqualTo("twoB2");

      final MyAutoB2OneImplied myAutoB2OneImplied = beanScope.get(MyAutoB2OneImplied.class);
      assertThat(myAutoB2OneImplied.one()).isEqualTo("oneB2");

      final MyAutoB2TwoExplicit myAutoB2TwoExplicit = beanScope.get(MyAutoB2TwoExplicit.class);
      assertThat(myAutoB2TwoExplicit.two()).isEqualTo("twoB2");
    }
  }

  @Test
  void test() {

    try (final BeanScope beanScope = BeanScope.builder().build()) {

      final MyAutoName myAutoName = beanScope.get(MyAutoName.class);
      assertThat(myAutoName.who()).isEqualTo("one");

      final MyAutoName2 myAutoName2 = beanScope.get(MyAutoName2.class);
      assertThat(myAutoName2.who()).isEqualTo("one");

      final MyGeneric<?> genericWild = beanScope.get(MyGeneric.class, "wild");
      final MyGeneric<?> generic2 = beanScope.get(MyAutoNameFactory_DI.TYPE_MyGenericString, "genericString");
      final MyGeneric<?> generic3 = beanScope.get(MyAutoNameFactory_DI.TYPE_MyGenericSome, "genericOther");
      assertThat(genericWild).isNotNull();
      assertThat(generic2).isNotNull();
      assertThat(generic3).isNotNull();
      assertThat(genericWild).isNotSameAs(generic2);
      assertThat(generic2).isNotSameAs(generic3);

      final MyAutoNameFactory factory = beanScope.get(MyAutoNameFactory.class);
      assertThat(factory.wiredSome()).isNotNull();
      assertThat(factory.wiredWild()).isNotNull();
      assertThat(factory.wiredWild2()).isNotNull();
      assertThat(factory.wiredWild2()).isSameAs(factory.wiredWild());
    }
  }
}
