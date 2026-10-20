package org.example.modules.db;

import io.avaje.inject.Bean;
import io.avaje.inject.Factory;

/**
 * Cannot be wired without external config, so any test that wires this module fails.
 */
@Factory
public class DbFactory {

  @Bean
  Database database() {
    throw new IllegalStateException("db.url is not set");
  }
}
