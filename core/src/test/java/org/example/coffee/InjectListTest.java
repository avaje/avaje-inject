package org.example.coffee;

import io.dinject.BeanContext;
import io.dinject.BootContext;
import org.example.coffee.list.CombinedSetSomei;
import org.example.coffee.list.CombinedSomei;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class InjectListTest {

  @Test
  public void test() {
    try (BeanContext context = new BootContext().load()) {
      CombinedSomei bean = context.getBean(CombinedSomei.class);
      List<String> somes = bean.lotsOfSomes();
      assertThat(somes).containsOnly("a", "b");
    }
  }

  @Test
  public void test_set() {
    try (BeanContext context = new BootContext().load()) {
      CombinedSetSomei bean = context.getBean(CombinedSetSomei.class);
      List<String> somes = bean.lotsOfSomes();
      assertThat(somes).containsOnly("a", "b");
    }
  }
}
