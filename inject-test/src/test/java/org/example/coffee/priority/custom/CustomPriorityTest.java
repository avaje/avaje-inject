package org.example.coffee.priority.custom;

import io.avaje.inject.ApplicationScope;
import io.avaje.inject.BeanScope;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CustomPriorityTest {

  @Test
  void test() {
    final BeanScope context = ApplicationScope.scope();

    final List<OtherIface> sorted = context.listByPriority(OtherIface.class, CustomPriority.class);

    assertThat(sorted.get(0)).isInstanceOf(COtheri.class);
    assertThat(sorted.get(1)).isInstanceOf(BOtheri.class);
    assertThat(sorted.get(2)).isInstanceOf(AOtheri.class);

  }
}
