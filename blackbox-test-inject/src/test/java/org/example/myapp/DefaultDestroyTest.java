package org.example.myapp;

import io.avaje.inject.BeanScope;
import io.ebean.Database;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultDestroyTest {

  @Test
  void expect_ebeanDatabase_hasShutdownByDefault() {
    try (BeanScope beanScope = BeanScope.builder().build()) {
      Database database = beanScope.get(Database.class);

      assertThat(database.isShutdown()).isFalse();
      beanScope.close();

      assertThat(database.isShutdown()).isTrue();
    }
  }
}
