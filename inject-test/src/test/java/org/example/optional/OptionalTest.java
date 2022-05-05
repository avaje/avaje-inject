package org.example.optional;

import io.avaje.inject.BeanScope;
import org.example.coffee.factory.Otherthing;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class OptionalTest {

  @Test
  void empty() {
    try (BeanScope scope = BeanScope.builder().build()) {

      Optional<IllegalStateException> orEmpty = scope.getOptional(IllegalStateException.class);
      assertThat(orEmpty).isEmpty();

      Optional<OptBax> foo = scope.getOptional(OptBax.class);
      assertThat(foo).isPresent();
    }
  }

  @Test
  void empty_withName() {
    try (BeanScope scope = BeanScope.builder().build()) {

      Optional<IllegalStateException> orEmpty = scope.getOptional(IllegalStateException.class, null);
      assertThat(orEmpty).isEmpty();

      Optional<OFooService> foo = scope.getOptional(Otherthing.class, "blue");
      assertThat(foo).isPresent();
    }
  }
}
