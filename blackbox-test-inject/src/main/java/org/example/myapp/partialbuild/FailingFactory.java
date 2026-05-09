package org.example.myapp.partialbuild;

import java.util.concurrent.atomic.AtomicBoolean;

import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import io.avaje.inject.Profile;

/**
 * Test fixture for verifying that AutoCloseable beans are closed when a
 * {@code @Factory} method throws partway through wiring.
 */
@Factory
@Profile("partial-build-failure-test")
public class FailingFactory {

  public static final AtomicBoolean FIRST_CLOSED = new AtomicBoolean();

  @Bean
  First first() {
    return new First();
  }

  @Bean
  String second(First f) {
    throw new IllegalStateException("boom while wiring String from " + f);
  }

  public static final class First implements AutoCloseable {
    @Override
    public void close() {
      FIRST_CLOSED.set(true);
    }
  }
}
