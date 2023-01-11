package org.example.myapp.config;

import io.avaje.inject.BeanScope;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class DupShortNameFactoryTest {

  @Test
  void factoryBeanRequiringFullyQualifiedName() {
    try (BeanScope testScope = BeanScope.builder().build()) {
      // both have short name of MyDup
      var one = testScope.get(org.example.myapp.config.MyDup.class);
      var two = testScope.get(DupShortNameFactory.MyDup.class);

      assertNotNull(one);
      assertNotNull(two);
      assertThat(one).isNotSameAs(two);
    }
  }
}
