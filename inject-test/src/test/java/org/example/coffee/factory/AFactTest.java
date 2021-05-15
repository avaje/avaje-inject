package org.example.coffee.factory;

import io.avaje.inject.BeanScope;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AFactTest {

  @Test
  public void postConstruct() {

    AFact bean;
    try (BeanScope context = BeanScope.newBuilder().build()) {
      bean = context.getBean(AFact.class);

      assertThat(bean.getCountConstruct()).isEqualTo(1);
      assertThat(bean.getCountDestroy()).isEqualTo(0);
    }
    assertThat(bean.getCountConstruct()).isEqualTo(1);
    assertThat(bean.getCountDestroy()).isEqualTo(1);
  }
}
