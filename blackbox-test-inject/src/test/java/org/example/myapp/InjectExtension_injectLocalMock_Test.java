package org.example.myapp;

import io.avaje.inject.test.InjectExtension;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(InjectExtension.class)
class InjectExtension_injectLocalMock_Test {

  @Inject HelloService helloService;

  @Mock HelloData mockData;

  @BeforeEach
  void before() {
    when(mockData.helloData()).thenReturn("LocalMock");
  }

  @Test
  void hello_1() {
    assertEquals("hello+LocalMock", helloService.hello());
  }

  @Test
  void hello_2() {
    when(mockData.helloData()).thenReturn("LocalMock_2");
    assertEquals("hello+LocalMock_2", helloService.hello());
  }

  @Test
  void hello_3() {
    when(mockData.helloData()).thenReturn("LocalMock_3");
    assertEquals("hello+LocalMock_3", helloService.hello());
  }
}
