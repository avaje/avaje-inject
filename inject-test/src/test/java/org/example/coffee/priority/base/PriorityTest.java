package org.example.coffee.priority.base;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.example.coffee.priority.base.PriorityFactory.DBasei;
import org.junit.jupiter.api.Test;

import io.avaje.inject.xtra.ApplicationScope;

class PriorityTest {

  @Test
  void listByPriority() {
    final List<BaseIface> sorted = ApplicationScope.listByPriority(BaseIface.class);
    assertExpectedOrder(sorted);
  }

  @Test
  void testGet() {
    assertThat(ApplicationScope.get(BaseIface.class)).isInstanceOf(CBasei.class);
  }

  private void assertExpectedOrder(List<BaseIface> sorted) {
    assertThat(sorted.get(0)).isInstanceOf(CBasei.class);
    assertThat(sorted.get(1)).isInstanceOf(BBasei.class);
    assertThat(sorted.get(2)).isInstanceOf(ABasei.class);
    assertThat(sorted.get(3)).isInstanceOf(DBasei.class);
  }
}
