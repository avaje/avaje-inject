package org.example.coffee.qualifier;

import io.avaje.inject.BeanContext;
import io.avaje.inject.BeanContextBuilder;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class StoreManagerWithNamedTest {

  @Test
  public void test() {

    try (BeanContext context = new BeanContextBuilder().build()) {
      StoreManagerWithNamed manager = context.getBean(StoreManagerWithNamed.class);
      String store = manager.store();
      assertThat(store).isEqualTo("blue");
    }
  }
}
