package org.example.modules;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.example.modules.client.CustomerClient;
import org.example.modules.db.Database;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import io.avaje.inject.test.InjectTest;
import jakarta.inject.Inject;

/** A test with mocks builds its own scope, which also uses the {@code @TestModules} default. */
@InjectTest
class MockInjectTest {

  @Mock CustomerClient client;
  @Inject TestSettings settings;
  @Inject Optional<Database> database;

  @Test
  void mockReplacesTheDefaultModuleBean() {
    when(client.customer(1)).thenReturn("mocked");
    assertThat(client.customer(1)).isEqualTo("mocked");
    assertThat(settings.name()).isEqualTo("test");
    assertThat(database).isEmpty();
  }
}
