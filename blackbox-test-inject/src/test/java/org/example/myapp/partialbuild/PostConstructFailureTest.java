package org.example.myapp.partialbuild;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import io.avaje.inject.BeanScope;

class PostConstructFailureTest {

  /**
   * When a {@code @PostConstruct} method throws after all beans have been
   * constructed, AutoCloseable beans that were already registered must still
   * be closed. Otherwise they leak resources.
   */
  @Test
  void postConstructFailureClosesAlreadyConstructedAutoCloseables() {
    PostConstructFailingFactory.CRASHER_CLOSED.set(false);

    assertThatThrownBy(() -> BeanScope.builder().profiles("partial-build-postconstruct-test").build())
        .hasMessageContaining("boom in postconstruct");

    assertThat(PostConstructFailingFactory.CRASHER_CLOSED)
        .as("AutoCloseable beans must be closed when a @PostConstruct throws and aborts the build")
        .isTrue();
  }
}
