package org.example.coffee.qualifier;

import io.dinject.BeanContext;
import io.dinject.BootContext;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class StoreManagerWithFieldQualifierTest {

  @Test
  public void test() {

    try (BeanContext context = new BootContext().load()) {
      StoreManagerWithFieldQualifier manager = context.getBean(StoreManagerWithFieldQualifier.class);
      String store = manager.store();
      assertThat(store).isEqualTo("blue");
    }
  }
}
