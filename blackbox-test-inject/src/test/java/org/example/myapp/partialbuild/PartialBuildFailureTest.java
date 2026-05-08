package org.example.myapp.partialbuild;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import io.avaje.inject.BeanScope;

class PartialBuildFailureTest {

  /**
   * When a factory method throws partway through wiring, AutoCloseable beans
   * that were already constructed and registered must still be closed.
   * Otherwise they leak resources (file handles, sockets, threads, etc.).
   */
  @Test
  void buildFailureClosesAlreadyConstructedAutoCloseables() {
    FailingFactory.FIRST_CLOSED.set(false);

    assertThatThrownBy(() -> BeanScope.builder().profiles("partial-build-failure-test").build())
        .hasMessageContaining("boom");

    assertThat(FailingFactory.FIRST_CLOSED)
        .as("AutoCloseable beans constructed before the failing factory must be closed when the build aborts")
        .isTrue();
  }
}
