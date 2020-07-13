package org.example.coffee.factory;

import io.dinject.BeanContext;
import io.dinject.BootContext;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MyFactoryTest {

  @Test
  public void methodsCalled() {
    try (BeanContext context = new BootContext().load()) {
      final MyFactory myFactory = context.getBean(MyFactory.class);
      assertThat(myFactory.methodsCalled()).contains("|useCFact", "|anotherCFact");
    }
  }
}
