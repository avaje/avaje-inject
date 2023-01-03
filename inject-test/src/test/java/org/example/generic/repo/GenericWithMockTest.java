package org.example.generic.repo;

import io.avaje.inject.BeanScope;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GenericWithMockTest {

  @Test
  void withMock() {
    try (BeanScope beanScope = BeanScope.builder().forTesting()
      .mock(MapRepo1.class)
      .build()) {

      ServiceClass serviceClass = beanScope.get(ServiceClass.class);
      MapRepo1 mapRepo1 = beanScope.get(MapRepo1.class);
      assertThat(mapRepo1.get()).isNull();

      MapRepo2 mapRepo2 = beanScope.get(MapRepo2.class);
      assertThat(mapRepo2.get()).isNotNull();
      assertThat(serviceClass.map2.get()).isNotNull();
    }
  }

  @Test
  void wireNoMocks() {
    try (BeanScope beanScope = BeanScope.builder().build()) {
      var serviceClass = beanScope.get(ServiceClass.class);
      String value = serviceClass.process();

      assertThat(value).isEqualTo("{=Model1}|{=Model2}");
    }
  }
}
