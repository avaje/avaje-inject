package org.example.coffee.priority.base;

import io.avaje.inject.ApplicationScope;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class PriorityTest {

  @Test
  void listByPriority() {
    final List<BaseIface> sorted = ApplicationScope.listByPriority(BaseIface.class);
    assertExpectedOrder(sorted);
  }

  private void assertExpectedOrder(List<BaseIface> sorted) {
    assertThat(sorted.get(0)).isInstanceOf(CBasei.class);
    assertThat(sorted.get(1)).isInstanceOf(BBasei.class);
    assertThat(sorted.get(2)).isInstanceOf(ABasei.class);
  }
}
