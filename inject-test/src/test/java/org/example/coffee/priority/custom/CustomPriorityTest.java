package org.example.coffee.priority.custom;

import io.avaje.inject.BeanContext;
import io.avaje.inject.SystemContext;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CustomPriorityTest {

  @Test
  void test() {
    final BeanContext context = SystemContext.context();

    final List<OtherIface> beans = context.getBeans(OtherIface.class);
    final List<OtherIface> sorted = context.sortByPriority(beans, CustomPriority.class);

    assertThat(sorted.get(0)).isInstanceOf(COtheri.class);
    assertThat(sorted.get(1)).isInstanceOf(BOtheri.class);
    assertThat(sorted.get(2)).isInstanceOf(AOtheri.class);

  }
}
