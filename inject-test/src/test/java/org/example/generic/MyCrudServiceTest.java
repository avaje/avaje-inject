package org.example.generic;

import io.avaje.inject.BeanScope;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class MyCrudServiceTest {

  @Test
  void test_genericTypesInjectedAsExpected() {
    try (BeanScope beanScope = BeanScope.builder().build()) {
      MyReadService readService = beanScope.get(MyReadService.class);
      MyCreateService createService = beanScope.get(MyCreateService.class);
      MyCrudService crudService = beanScope.get(MyCrudService.class);

      assertNotNull(readService);
      assertNotNull(createService);
      assertNotNull(crudService);

      assertThat(crudService.create).isSameAs(createService);
      assertThat(crudService.read).isSameAs(readService);
    }
  }
}
