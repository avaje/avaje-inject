package org.example.modules;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.example.modules.audit.AuditClient;
import org.example.modules.audit.AuditModule;
import org.example.modules.client.CustomerClient;
import org.junit.jupiter.api.Test;

import io.avaje.inject.test.InjectTest;
import jakarta.inject.Inject;

/** {@code @InjectTest(modules = ...)} overrides the {@code @TestModules} default. */
@InjectTest(modules = AuditModule.class)
class OverrideModulesTest {

  @Inject AuditClient auditClient;
  @Inject Optional<CustomerClient> client;

  @Test
  void wiresOnlyTheSelectedModules() {
    assertThat(auditClient).isNotNull();
    assertThat(client).isEmpty();
  }
}
