package org.example.coffee.qualifier;

import io.dinject.BeanContext;
import io.dinject.BeanContextBuilder;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class StoreManagerWithQualifierTest {

  @Test
  public void test() {

    try (BeanContext context = new BeanContextBuilder().build()) {
      StoreManagerWithQualifier manager = context.getBean(StoreManagerWithQualifier.class);
      String store = manager.store();
      assertThat(store).isEqualTo("blue");
    }
  }
}
