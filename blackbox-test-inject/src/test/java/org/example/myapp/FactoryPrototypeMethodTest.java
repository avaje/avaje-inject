package org.example.myapp;

import io.avaje.inject.BeanScope;
import org.example.myapp.config.AppConfig;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FactoryPrototypeMethodTest {

  @Test
  void test() {
    try (BeanScope beanScope = BeanScope.builder().build()) {

      AppConfig.Builder one = beanScope.get(AppConfig.Builder.class);
      AppConfig.Builder two = beanScope.get(AppConfig.Builder.class);
      assertThat(one).isNotNull();
      assertThat(one).isNotSameAs(two);

      AppConfig.BuilderUser builderUser = beanScope.get(AppConfig.BuilderUser.class);
      AppConfig.Builder b0 = builderUser.createBuilder();
      AppConfig.Builder b1 = builderUser.createBuilder();

      assertThat(b0).isNotNull();
      assertThat(b0).isNotSameAs(b1);
    }
  }
}
