package org.example.coffee;

import io.avaje.inject.BeanContext;
import io.avaje.inject.BeanContextBuilder;
import org.example.coffee.factory.BFact;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class FactoryTest {

  @Test
  public void test() {

    try (BeanContext context = new BeanContextBuilder()
      .build()) {

      BFact bean = context.getBean(BFact.class);
      String b =  bean.b();
      assertThat(b).isNotNull();
    }

  }
}
