package org.example.myapp.profileorder;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import io.avaje.config.Config;
import io.avaje.inject.BeanScope;

/**
 * When multiple beans implement the same interface and are selected by profile, every one of them
 * must be wired before any consumer of that interface.
 *
 * <p>See https://github.com/avaje/avaje-inject/issues/1049
 */
class ProfileOrderTest {

  @AfterEach
  void clearConfig() {
    Config.clearProperty("avaje.profiles");
  }

  @Test
  void cloudProfile_dbServiceWiredBeforeConsumer() {
    Config.setProperty("avaje.profiles", "cloud");

    try (BeanScope beanScope = BeanScope.builder().build()) {
      assertThat(beanScope.get(ServiceConsumer.class).serviceName()).isEqualTo("db");
    }
  }

  @Test
  void defaultProfile_fileServiceWiredBeforeConsumer() {
    try (BeanScope beanScope = BeanScope.builder().build()) {
      assertThat(beanScope.get(ServiceConsumer.class).serviceName()).isEqualTo("file");
    }
  }
}
