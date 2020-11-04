package org.example.coffee.qualifier;

import io.avaje.inject.BeanContext;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class StoreManagerWithNamedTest {

  @Test
  public void test() {

    try (BeanContext context = BeanContext.newBuilder().build()) {
      StoreManagerWithNamed manager = context.getBean(StoreManagerWithNamed.class);
      String store = manager.store();
      assertThat(store).isEqualTo("blue");
    }
  }
}
