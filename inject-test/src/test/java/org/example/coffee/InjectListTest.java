package org.example.coffee;

import io.avaje.inject.BeanContext;
import org.example.coffee.list.CombinedSetSomei;
import org.example.coffee.list.CombinedSomei;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class InjectListTest {

  @Test
  public void test() {
    try (BeanContext context = BeanContext.newBuilder().build()) {
      CombinedSomei bean = context.getBean(CombinedSomei.class);
      List<String> somes = bean.lotsOfSomes();
      assertThat(somes).containsOnly("a", "b", "a2");
    }
  }

  @Test
  public void test_set() {
    try (BeanContext context = BeanContext.newBuilder().build()) {
      CombinedSetSomei bean = context.getBean(CombinedSetSomei.class);
      List<String> somes = bean.lotsOfSomes();
      assertThat(somes).containsOnly("a", "b", "a2");
    }
  }
}
