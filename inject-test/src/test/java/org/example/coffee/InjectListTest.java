package org.example.coffee;

import io.avaje.inject.BeanScope;
import org.example.coffee.list.CombinedSetSomei;
import org.example.coffee.list.CombinedSomei;
import org.example.coffee.list.SomeInterfaceConsumer;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class InjectListTest {

  @Test
  void test() {
    try (BeanScope context = BeanScope.builder().build()) {
      CombinedSomei bean = context.get(CombinedSomei.class);
      List<String> somes = bean.lotsOfSomes();
      assertThat(somes).containsOnly("a", "b", "a2");
    }
  }

  @Test
  void test_set() {
    try (BeanScope context = BeanScope.builder().build()) {
      CombinedSetSomei bean = context.get(CombinedSetSomei.class);
      List<String> somes = bean.lotsOfSomes();
      assertThat(somes).containsOnly("a", "b", "a2");
    }
  }

  @Test
  void testEmptyList() {
    try (BeanScope context = BeanScope.builder().build()) {
      SomeInterfaceConsumer consumer = context.get(SomeInterfaceConsumer.class);
      assertThat(consumer.getList()).isEmpty();
      System.out.println("--------------------> works");
    }
  }
}
