package org.example.myapp.config;

import io.avaje.inject.BeanScope;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AppConfigTest {

  @Test
  void factorySecondaryAsProvider() {
    try (BeanScope testScope = BeanScope.builder().build()) {
      assertNotNull(testScope.get(AppConfig.class));

      var one = testScope.get(AppConfig.MySecType.class);
      var two = testScope.get(AppConfig.MySecType.class);
      assertThat(one).describedAs("Secondary Bean provided once").isSameAs(two);

      var optOne = testScope.get(AppConfig.MySecOptType.class);
      var optTwo = testScope.get(AppConfig.MySecOptType.class);
      assertThat(optOne).describedAs("Secondary optional Bean provided once").isSameAs(optTwo);

      var myPrim = testScope.get(AppConfig.MyPrim.class);
      assertThat(myPrim.val).isEqualTo("prime");

      var notPrimary = testScope.get(AppConfig.MyPrim.class, "notPrimary");
      assertThat(notPrimary.val).isEqualTo("notPrimary");

      List<AppConfig.MyPrim> list = testScope.list(AppConfig.MyPrim.class);
      assertThat(list).hasSize(2);
    }
  }

  @Test
  void beanWithAutoCloseable() {
    try (BeanScope testScope = BeanScope.builder().build()) {
      final var someInterface = testScope.get(AppConfig.SomeInterface.class);
      assertThat(someInterface).isInstanceOf(AutoCloseable.class);
      assertThat(AppConfig.BEAN_AUTO_CLOSED.get()).isFalse();
    }
    assertThat(AppConfig.BEAN_AUTO_CLOSED.get()).isTrue();
  }
}
