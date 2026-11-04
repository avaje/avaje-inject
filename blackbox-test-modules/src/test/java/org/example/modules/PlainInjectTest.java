package org.example.modules;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.example.modules.audit.AuditClient;
import org.example.modules.client.CustomerClient;
import org.example.modules.db.Database;
import org.example.modules.report.ReportClient;
import org.junit.jupiter.api.Test;

import io.avaje.inject.test.InjectTest;
import jakarta.inject.Inject;

/**
 * A plain {@code @InjectTest} uses the {@code @TestModules} default, from both {@code TestConfig}
 * and the package-info.
 */
@InjectTest
class PlainInjectTest {

  @Inject CustomerClient client;
  @Inject ReportClient reportClient;
  @Inject TestSettings settings;
  @Inject Optional<Database> database;
  @Inject Optional<AuditClient> auditClient;

  @Test
  void wiresTheDefaultModules() {
    assertThat(client.customer(42)).isEqualTo("customer-42");
    assertThat(reportClient).isNotNull();
    assertThat(settings.name()).isEqualTo("test");
  }

  @Test
  void otherModulesAreNotWired() {
    assertThat(database).isEmpty();
    assertThat(auditClient).isEmpty();
  }
}
