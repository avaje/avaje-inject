package org.example.myapp.duplicate;

import io.avaje.inject.BeanScope;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class SameTypeTest {

  @Test
  void testDuplicateShortName() {
    try (BeanScope testScope = BeanScope.builder().build()) {

      var sameType = testScope.get(SameType.class);
      var sameTypeInner = testScope.get(SameType.Inner.class);
      var sameType2 = testScope.get(org.example.myapp.duplicate.two.SameType.class);
      var sameType2Inner = testScope.get(org.example.myapp.duplicate.two.SameType.Inner.class);

      assertThat(sameType).isNotNull();
      assertThat(sameTypeInner).isNotNull();
      assertThat(sameType2).isNotNull();
      assertThat(sameType2Inner).isNotNull();
    }
  }
}
