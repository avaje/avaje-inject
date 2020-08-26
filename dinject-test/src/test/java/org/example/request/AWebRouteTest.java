package org.example.request;


import io.dinject.SystemContext;
import io.javalin.http.Context;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class AWebRouteTest {

  @Test
  public void test() {

    final AWebRoute route = SystemContext.getBean(AWebRoute.class);

    Context ctx = Mockito.mock(Context.class);
    when(ctx.toString()).thenReturn("a");
    assertEquals("hi a!", route.get(ctx));

    Context ctx2 = Mockito.mock(Context.class);
    when(ctx2.toString()).thenReturn("b");
    assertEquals("hi b!", route.get(ctx2));
  }
}
