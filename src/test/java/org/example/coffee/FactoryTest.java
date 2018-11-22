package org.example.coffee;

import io.dinject.BeanContext;
import io.dinject.BootContext;
import org.example.coffee.factory.BFact;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class FactoryTest {

  @Test
  public void test() {

    try (BeanContext context = new BootContext()
      .load()) {

      BFact bean = context.getBean(BFact.class);
      String b =  bean.b();
      assertThat(b).isNotNull();
    }

  }
}
