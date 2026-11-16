package org.example.modules;

import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import io.avaje.inject.test.TestModules;
import io.avaje.inject.test.TestScope;
import org.example.modules.client.ClientModule;

/**
 * By default tests only wire the module under test, and not the default module with the Database
 * that cannot be wired in tests.
 */
@TestScope
@Factory
@TestModules(ClientModule.class)
class TestConfig {

  @Bean
  TestSettings settings() {
    return new TestSettings("test");
  }
}
