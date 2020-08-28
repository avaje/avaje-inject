package org.example.coffee.factory;

import io.avaje.inject.BeanContext;
import io.avaje.inject.BeanContextBuilder;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MyFactoryTest {

  @Test
  public void methodsCalled() {
    try (BeanContext context = new BeanContextBuilder().build()) {
      final MyFactory myFactory = context.getBean(MyFactory.class);
      assertThat(myFactory.methodsCalled()).contains("|useCFact", "|anotherCFact");
    }
  }
}
