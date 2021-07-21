package org.example.coffee.qualifier;

import io.avaje.inject.BeanScope;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class StoreManagerWithFieldQualifierTest {

  @Test
  public void test() {

    try (BeanScope context = BeanScope.newBuilder().build()) {
      StoreManagerWithFieldQualifier manager = context.get(StoreManagerWithFieldQualifier.class);
      String store = manager.store();
      assertThat(store).isEqualTo("blue");
    }
  }
}
