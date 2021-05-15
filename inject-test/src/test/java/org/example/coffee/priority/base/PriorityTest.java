package org.example.coffee.priority.base;

import io.avaje.inject.BeanScope;
import io.avaje.inject.SystemContext;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class PriorityTest {

  @Test
  void getBeansByPriority() {
    final BeanScope context = SystemContext.context();

    final List<BaseIface> beans = context.getBeansByPriority(BaseIface.class);
    assertExpectedOrder(beans);
  }

  @Test
  void sortByPriority() {
    final BeanScope context = SystemContext.context();

    final List<BaseIface> beans = context.getBeans(BaseIface.class);
    final List<BaseIface> sorted = context.sortByPriority(beans);

    assertExpectedOrder(sorted);
  }

  private void assertExpectedOrder(List<BaseIface> sorted) {
    assertThat(sorted.get(0)).isInstanceOf(CBasei.class);
    assertThat(sorted.get(1)).isInstanceOf(BBasei.class);
    assertThat(sorted.get(2)).isInstanceOf(ABasei.class);
  }
}
