package org.example.modules;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.example.modules.client.CustomerClient;
import org.example.modules.db.Database;
import org.junit.jupiter.api.Test;

import io.avaje.inject.test.InjectTest;
import jakarta.inject.Inject;

/** The class level scope also uses the {@code @TestModules} default. */
@InjectTest
class StaticInjectTest {

  @Inject static CustomerClient client;
  @Inject static Optional<Database> database;

  @Test
  void wiresTheDefaultModules() {
    assertThat(client).isNotNull();
    assertThat(database).isEmpty();
  }
}
