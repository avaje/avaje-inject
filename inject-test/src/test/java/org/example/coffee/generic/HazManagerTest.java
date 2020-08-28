package org.example.coffee.generic;

import io.avaje.inject.BeanContext;
import io.avaje.inject.BeanContextBuilder;
import io.avaje.inject.SystemContext;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

public class HazManagerTest {

  @Test
  public void find_when_allWired() {

    HazManager hazManager = SystemContext.getBean(HazManager.class);
    Haz haz = hazManager.find(42L);

    assertThat(haz.id).isEqualTo(42L);
  }

  @Test
  public void fin_with_mockHaz() {

    try (BeanContext context = new BeanContextBuilder()
      .withMock(HazRepo.class)
      .build()) {

      HazManager hazManager = context.getBean(HazManager.class);
      Haz haz = hazManager.find(42L);

      assertThat(haz).isNull();
    }
  }

  @Test
  public void find_with_stubHazUsingMockito() {

    try (BeanContext context = new BeanContextBuilder()
      .withMock(HazRepo.class, hazRepo -> {
        when(hazRepo.findById(anyLong())).thenReturn(new Haz(-23L));
      })
      .build()) {

      HazManager hazManager = context.getBean(HazManager.class);
      Haz haz = hazManager.find(42L);

      assertThat(haz.id).isEqualTo(-23L);
    }
  }

  @Test
  public void find_with_testDouble() {

    TDHazRepo testDouble = new TDHazRepo();

    try (BeanContext context = new BeanContextBuilder()
      .withBeans(testDouble)
      .build()) {

      HazManager hazManager = context.getBean(HazManager.class);

      Haz haz = hazManager.find(42L);
      assertThat(haz.id).isEqualTo(64L);

      testDouble.id = 48L;
      haz = hazManager.find(42L);
      assertThat(haz.id).isEqualTo(48L);

      testDouble.id = 128L;
      haz = hazManager.find(42L);
      assertThat(haz.id).isEqualTo(128L);

    }
  }

  /**
   * Test double for HazRepo - nice when we want interesting test behaviour.
   */
  class TDHazRepo extends HazRepo {

    long id = 64L;

    @Override
    public Haz findById(Long paramId) {
      return new Haz(id);
    }
  }
}
