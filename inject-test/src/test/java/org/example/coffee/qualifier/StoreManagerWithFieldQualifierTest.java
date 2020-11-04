package org.example.coffee.qualifier;

import io.avaje.inject.BeanContext;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class StoreManagerWithFieldQualifierTest {

  @Test
  public void test() {

    try (BeanContext context = BeanContext.newBuilder().build()) {
      StoreManagerWithFieldQualifier manager = context.getBean(StoreManagerWithFieldQualifier.class);
      String store = manager.store();
      assertThat(store).isEqualTo("blue");
    }
  }
}
