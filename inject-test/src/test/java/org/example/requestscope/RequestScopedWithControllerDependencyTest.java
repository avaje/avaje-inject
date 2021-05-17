package org.example.requestscope;

import io.avaje.inject.ApplicationScope;
import io.avaje.inject.RequestScope;
import io.javalin.http.Context;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Test that the factory generation is suppressed for the case where
 * the request scoped bean has a dependency on one of the special
 * 'request scope controller dependencies' (like Javalin Context).
 */
class RequestScopedWithControllerDependencyTest {

  @Test
  void test() {
    try (RequestScope scope = ApplicationScope.newRequestScope()
      .withBean(Context.class, mock(Context.class))
      .build()) {

      final MyReqThingWithContext reqThing = scope.get(MyReqThingWithContext.class);
      assertThat(reqThing.javalinContext).isNotNull();
    }
  }
}
