package org.example.myapp.lastditch;

import static org.assertj.core.api.Assertions.assertThat;

import org.example.myapp.profileorder.lastditch.Dante;
import org.junit.jupiter.api.Test;

import io.avaje.inject.BeanScope;

/**
 * Lust and Gluttony both implement Circle and both depend on Circle, and two such beans over the
 * same interface can not be wired by the strict rounds. The ordering therefore falls back to the
 * last ditch round, which treats a dependency as satisfied once any one of the beans providing it
 * is wired. That relaxation must not let Dante be ordered before Virgil, the only Guide active
 * outside the paradiso profile.
 *
 * <p>See https://github.com/avaje/avaje-inject/issues/1049
 */
class LastDitchOrderTest {

  @Test
  void consumerWiredAfterAllImplementationsOfItsDependency() {
    try (BeanScope beanScope = BeanScope.builder().build()) {
      assertThat(beanScope.get(Dante.class).guideName()).isEqualTo("virgil");
    }
  }
}
