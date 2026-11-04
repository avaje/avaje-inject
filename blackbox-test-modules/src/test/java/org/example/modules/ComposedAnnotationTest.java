package org.example.modules;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.example.modules.audit.AuditClient;
import org.example.modules.client.CustomerClient;
import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;

/** {@code @InjectTest} used as a meta-annotation. */
@AuditTest
class ComposedAnnotationTest {

  @Inject AuditClient auditClient;
  @Inject Optional<CustomerClient> client;

  @Test
  void usesTheModulesOfTheComposedAnnotation() {
    assertThat(auditClient).isNotNull();
    assertThat(client).isEmpty();
  }
}
