package org.example.myapp.named;

import io.avaje.inject.BeanScope;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MyNamedTest {

  @Test
  void namedWithHypens() {
    try (BeanScope beanScope = BeanScope.builder().build()) {

      MyNamed myNamed = beanScope.get(MyNamed.class);
      assertThat(myNamed).isNotNull();

      MyNamed myNamed2 = beanScope.get(MyNamed.class, "my-name-with-hyphens");
      assertThat(myNamed2).isNotNull();
      assertThat(myNamed2).isSameAs(myNamed);
    }

  }
}
