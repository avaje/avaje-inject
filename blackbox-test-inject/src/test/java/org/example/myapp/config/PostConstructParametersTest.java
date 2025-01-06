package org.example.myapp.config;

import io.avaje.inject.BeanScope;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PostConstructParametersTest {

  @Test
  void factorySecondaryAsProvider() {
    try (BeanScope testScope = BeanScope.builder().build()) {
      var one = testScope.get(LifeOne.class);
      var two = testScope.get(LifeTwo.class);
      var three = testScope.get(LifeThree.class);
      var four = testScope.get(LifeFour.class);

      assertThat(one._state).isEqualTo("post|scope");
      assertThat(two._state).isEqualTo("post|one|scope");
      assertThat(three._state).isEqualTo("post|one");
      assertThat(four._state).isEqualTo("post|one|two");
    }
  }

}
