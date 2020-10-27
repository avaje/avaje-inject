package io.avaje.inject.core;

import io.avaje.inject.BeanContext;
import io.avaje.inject.BeanContextBuilder;
import io.avaje.inject.SystemContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

public class NestedInterfaceTest {

  @Test
  void test() {
    final BeanContext context = SystemContext.context();
    final Some.Nested nestedInterface = context.getBean(Some.Nested.class);

    assertNotNull(nestedInterface);
    assertEquals("MySomeNested", nestedInterface.doNested());
  }

  @Test
  void test_provided() {

    try (BeanContext context = new BeanContextBuilder()
      .withMock(Some.Nested.class, nested -> when(nested.doNested()).thenReturn("myMock"))
      .build()) {

      final Some.Nested nestedInterface = context.getBean(Some.Nested.class);
      assertNotNull(nestedInterface);
      assertEquals("myMock", nestedInterface.doNested());
    }

  }

}
