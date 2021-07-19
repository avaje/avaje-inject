package org.example.iface;

import io.avaje.inject.ApplicationScope;
import io.avaje.inject.BeanScope;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

public class NestedInterfaceTest {

  @Test
  void test() {
    final Some.Nested nestedInterface = ApplicationScope.get(Some.Nested.class);

    assertNotNull(nestedInterface);
    assertEquals("MySomeNested", nestedInterface.doNested());
  }

  @Test
  void test_provided() {

    try (BeanScope context = BeanScope.newBuilder()
      .withMock(Some.Nested.class, nested -> when(nested.doNested()).thenReturn("myMock"))
      .build()) {

      final Some.Nested nestedInterface = context.get(Some.Nested.class);
      assertNotNull(nestedInterface);
      assertEquals("myMock", nestedInterface.doNested());
    }

  }

}
