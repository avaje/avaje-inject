package org.example.coffee.qualifier;

import io.dinject.BeanContext;
import io.dinject.BootContext;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class StoreManagerWithNamedTest {

  @Test
  public void test() {

    try (BeanContext context = new BootContext().load()) {
      StoreManagerWithNamed manager = context.getBean(StoreManagerWithNamed.class);
      String store = manager.store();
      assertThat(store).isEqualTo("blue");
    }
  }
}
