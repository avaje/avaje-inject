package org.example.coffee.fruit;

import io.avaje.inject.BeanScope;
import io.avaje.inject.BeanScopeBuilder;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;

public class AppleServiceTest {

  @Test
  public void test_spyWithFieldInjection() {

    BeanScopeBuilder contextBuilder = BeanScope.builder();
    contextBuilder.forTesting().withSpy(AppleService.class);

    try (BeanScope beanScope = contextBuilder.build()) {

      AppleService appleService = beanScope.get(AppleService.class);

      doNothing()
        .when(appleService)
        .apple(anyString(), anyString(), anyString());

      assertThat(appleService.bananaService).isNotNull();
      assertThat(appleService.peachService).isNotNull();

      appleService.apple("one", "two", "three");

      String out = appleService.ban();
      assertThat(out).isEqualTo("hello banana peach");
    }
  }

  @Test
  public void test_whenNoMockOrSpy() {

    try (BeanScope beanScope = BeanScope.builder().build()) {

      AppleService appleService = beanScope.get(AppleService.class);

      assertThat(appleService.bananaService).isNotNull();
      assertThat(appleService.peachService).isNotNull();

      appleService.apple("one", "two", "three");
    }
  }

}
