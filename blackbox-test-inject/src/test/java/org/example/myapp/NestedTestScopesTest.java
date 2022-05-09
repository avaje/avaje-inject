package org.example.myapp;

import io.avaje.inject.BeanScope;
import io.avaje.inject.test.TestBeanScope;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class NestedTestScopesTest {

  @Test
  void nestedScopes() {

    final BeanScope globalTestScope = TestBeanScope.initialise();

    try (BeanScope level1TestScope = TestBeanScope.builder()
      .forTesting()
      .mock(OtherService.class)
      .build()) {

      try (BeanScope level2TestScope = TestBeanScope.builder()
        .parent(level1TestScope, false)
        .forTesting()
        .mock(HelloService.class)
        .build()) {

        HelloData helloData = globalTestScope.get(HelloData.class);
        assertThat(helloData.helloData()).isEqualTo("TestHelloData");
        assertThat(globalTestScope.getOptional(OtherService.class)).isEmpty();
        assertThat(globalTestScope.getOptional(HelloService.class)).isEmpty();


        OtherService otherLevel1 = level1TestScope.get(OtherService.class);
        OtherService otherLevel2 = level2TestScope.get(OtherService.class);
        assertThat(otherLevel1).isSameAs(otherLevel2);

        HelloService helloLevel1 = level1TestScope.get(HelloService.class);
        HelloService helloLevel2 = level2TestScope.get(HelloService.class);

        assertThat(helloLevel1).isNotSameAs(helloLevel2);


        when(helloLevel2.hello()).thenReturn("stub");
        assertEquals("hello+TestHelloData", helloLevel1.hello());
        assertEquals("stub", helloLevel2.hello());
      }
    }
  }

}
