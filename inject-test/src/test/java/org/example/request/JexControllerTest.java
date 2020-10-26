package org.example.request;

import io.avaje.inject.SystemContext;
import io.avaje.jex.Context;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class JexControllerTest {

  @Test
  void uses_factory_taking_context() {

    assertNull(SystemContext.getBean(JexController.class));

    final JexController$factory factory = SystemContext.getBean(JexController$factory.class);
    final JexController jexController = factory.create(Mockito.mock(Context.class));

    assertNotNull(jexController);
    assertSame(factory.service0, jexController.service);
    assertSame(jexController.service, SystemContext.getBean(AService.class));
  }
}
