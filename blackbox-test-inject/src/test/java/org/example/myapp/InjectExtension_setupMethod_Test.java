package org.example.myapp;

import io.avaje.inject.BeanScopeBuilder;
import io.avaje.inject.test.InjectJunitExtension;
import io.avaje.inject.test.Setup;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(InjectJunitExtension.class)
class InjectExtension_setupMethod_Test {

  @Inject
  HelloService helloService;

  HelloData myTestDouble;

  @Setup
  void setup(BeanScopeBuilder builder) {
    myTestDouble = mock(HelloData.class);
    when(myTestDouble.helloData()).thenReturn("MockedViaSetupMethod");
    builder.bean(HelloData.class, myTestDouble);
  }

  @Test
  void hello_1() {
    assertEquals("hello+MockedViaSetupMethod", helloService.hello());
  }

  @Test
  void hello_2() {
    when(myTestDouble.helloData()).thenReturn("LocalMock_2");
    assertEquals("hello+LocalMock_2", helloService.hello());
  }

}
