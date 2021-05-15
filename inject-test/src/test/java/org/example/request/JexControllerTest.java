package org.example.request;

import io.avaje.inject.ApplicationScope;
import io.avaje.jex.Context;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

class JexControllerTest {

  @Test
  void uses_factory_taking_context() {

    assertNull(ApplicationScope.get(JexController.class));

    final JexController$factory factory = ApplicationScope.get(JexController$factory.class);
    final Context context = Mockito.mock(Context.class);
    final JexController jexController = factory.create(context);

    assertNotNull(jexController);
    assertSame(factory.service0, jexController.service);
    assertSame(jexController.service, ApplicationScope.get(AService.class));
    assertSame(jexController.context, context);
  }
}
