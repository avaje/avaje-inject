package org.example.request;


import io.avaje.inject.xtra.ApplicationScope;
import io.javalin.http.Context;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

public class AWebRouteTest {

  @Test
  public void test() {

    final AWebRoute route = ApplicationScope.get(AWebRoute.class);

    Context ctx = Mockito.mock(Context.class);
    when(ctx.toString()).thenReturn("a");
    assertEquals("hi a!", route.get(ctx));

    Context ctx2 = Mockito.mock(Context.class);
    when(ctx2.toString()).thenReturn("b");
    assertEquals("hi b!", route.get(ctx2));
  }
}
