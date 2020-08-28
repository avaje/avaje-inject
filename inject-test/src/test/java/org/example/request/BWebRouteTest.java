package org.example.request;


import io.avaje.inject.SystemContext;
import io.helidon.webserver.ServerRequest;
import io.helidon.webserver.ServerResponse;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

public class BWebRouteTest {

  @Test
  public void test() {

    final BWebRoute route = SystemContext.getBean(BWebRoute.class);

    ServerRequest req = mockReq("a");
    ServerResponse res = mockRes("x");
    assertEquals("hi ax!", route.get(req, res));

    ServerRequest req2 = mockReq("b");
    ServerResponse res2 = mockRes("y");
    assertEquals("hi by!", route.get(req2, res2));
  }

  private ServerRequest mockReq(String message) {
    final ServerRequest mock = Mockito.mock(ServerRequest.class);
    when(mock.toString()).thenReturn(message);
    return mock;
  }

  private ServerResponse mockRes(String message) {
    final ServerResponse mock = Mockito.mock(ServerResponse.class);
    when(mock.toString()).thenReturn(message);
    return mock;
  }
}
