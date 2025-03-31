package org.example.request;

import io.avaje.inject.xtra.ApplicationScope;
import io.avaje.jex.http.Context;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class JexControllerTest {

  @Test
  void uses_factory_taking_context() {

    assertThrows(NoSuchElementException.class, () -> ApplicationScope.get(JexController.class));

    final JexController$Factory factory = ApplicationScope.get(JexController$Factory.class);
    final Context context = Mockito.mock(Context.class);
    final JexController jexController = factory.create(context);

    assertNotNull(jexController);
    assertSame(factory.service0, jexController.service);
    assertSame(jexController.service, ApplicationScope.get(AService.class));
    assertSame(jexController.context, context);
  }
}
