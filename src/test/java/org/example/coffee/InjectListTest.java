package org.example.coffee;

import io.kanuka.BeanContext;
import io.kanuka.BootContext;
import org.example.coffee.list.CombinedSomei;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class InjectListTest {

  @Test
  public void test() {

    try (BeanContext context = new BootContext().load()) {

      CombinedSomei bean = context.getBean(CombinedSomei.class);
      String other = bean.lotsOfSomes();
      assertThat(other).isEqualTo("a,b");
    }
  }
}
