package org.example.coffee.qualifier;

import io.avaje.inject.BeanScope;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class StoreManagerWithQualifierTest {

  @Test
  public void test() {

    try (BeanScope context = BeanScope.builder().build()) {
      StoreManagerWithQualifier manager = context.get(StoreManagerWithQualifier.class);
      String store = manager.store();
      assertThat(store).isEqualTo("blue");
    }
  }
}
