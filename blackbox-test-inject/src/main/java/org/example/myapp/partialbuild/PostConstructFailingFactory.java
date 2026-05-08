package org.example.myapp.partialbuild;

import java.util.concurrent.atomic.AtomicBoolean;

import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import io.avaje.inject.PostConstruct;
import io.avaje.inject.Profile;

/**
 * Test fixture for verifying that AutoCloseable beans are closed when a
 * {@code @PostConstruct} method throws after all beans have been wired.
 */
@Factory
@Profile("partial-build-postconstruct-test")
public class PostConstructFailingFactory {

  public static final AtomicBoolean CRASHER_CLOSED = new AtomicBoolean();

  @Bean
  Crasher crasher() {
    return new Crasher();
  }

  public static final class Crasher implements AutoCloseable {

    @PostConstruct
    void start() {
      throw new IllegalStateException("boom in postconstruct");
    }

    @Override
    public void close() {
      CRASHER_CLOSED.set(true);
    }
  }
}
