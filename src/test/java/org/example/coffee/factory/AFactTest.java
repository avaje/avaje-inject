package org.example.coffee.factory;

import io.dinject.BeanContext;
import io.dinject.BootContext;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AFactTest {

  @Test
  public void postConstruct() {

    AFact bean;
    try (BeanContext context = new BootContext()
      .load()) {

      bean = context.getBean(AFact.class);

      assertThat(bean.getCountConstruct()).isEqualTo(1);
      assertThat(bean.getCountDestroy()).isEqualTo(0);
    }
    assertThat(bean.getCountConstruct()).isEqualTo(1);
    assertThat(bean.getCountDestroy()).isEqualTo(1);
  }
}
