package org.example.coffee.fruit;

import io.dinject.BeanContext;
import io.dinject.BootContext;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;

public class AppleServiceTest {

  @Test
  public void test_spyWithFieldInjection() {

    BootContext bootContext = new BootContext();
    bootContext.withSpy(AppleService.class);

    try (BeanContext beanContext = bootContext.load()) {

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

    BootContext bootContext = new BootContext();

    try (BeanContext beanContext = bootContext.load()) {

      AppleService appleService = beanContext.getBean(AppleService.class);

      assertThat(appleService.bananaService).isNotNull();
      assertThat(appleService.peachService).isNotNull();

      appleService.apple("one", "two", "three");
    }
  }

}
