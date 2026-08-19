package org.example.condorder;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.avaje.inject.BeanScope;
import org.example.condorder.m1.M1Module;
import org.example.condorder.m1.Reader;
import org.example.condorder.m2.M2Module;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class ConditionalProviderOrderTest {

  @AfterEach
  void clear() {
    System.clearProperty("greet.mode");
  }

  @Test
  void conditionFalse_consumerModuleFirst() {
    System.setProperty("greet.mode", "alt");
    try (BeanScope scope = BeanScope.builder().modules(new M1Module(), new M2Module()).build()) {
      assertEquals("alt", scope.get(Reader.class).read());
    }
  }

  @Test
  void conditionFalse_providerModuleFirst() {
    System.setProperty("greet.mode", "alt");
    try (BeanScope scope = BeanScope.builder().modules(new M2Module(), new M1Module()).build()) {
      assertEquals("alt", scope.get(Reader.class).read());
    }
  }

  @Test
  void conditionTrue_eitherOrder() {
    System.setProperty("greet.mode", "app");
    try (BeanScope scope = BeanScope.builder().modules(new M1Module(), new M2Module()).build()) {
      assertEquals("app", scope.get(Reader.class).read());
    }
    try (BeanScope scope = BeanScope.builder().modules(new M2Module(), new M1Module()).build()) {
      assertEquals("app", scope.get(Reader.class).read());
    }
  }

  @Test
  void conditionFalse_classpathDiscovery() {
    System.setProperty("greet.mode", "alt");
    try (BeanScope scope = BeanScope.builder().build()) {
      assertEquals("alt", scope.get(Reader.class).read());
    }
  }
}
