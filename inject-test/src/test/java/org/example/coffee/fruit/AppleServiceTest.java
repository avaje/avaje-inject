package org.example.coffee.fruit;

import io.avaje.inject.BeanContext;
import io.avaje.inject.BeanContextBuilder;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;

public class AppleServiceTest {

  @Test
  public void test_spyWithFieldInjection() {

    BeanContextBuilder contextBuilder = BeanContext.newBuilder();
    contextBuilder.withSpy(AppleService.class);

    try (BeanContext beanContext = contextBuilder.build()) {

      AppleService appleService = beanContext.getBean(AppleService.class);

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

    try (BeanContext beanContext = BeanContext.newBuilder().build()) {

      AppleService appleService = beanContext.getBean(AppleService.class);

      assertThat(appleService.bananaService).isNotNull();
      assertThat(appleService.peachService).isNotNull();

      appleService.apple("one", "two", "three");
    }
  }

}
