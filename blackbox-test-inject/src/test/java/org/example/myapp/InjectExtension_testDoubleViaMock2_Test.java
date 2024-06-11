package org.example.myapp;

import io.avaje.inject.test.InjectExtension;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(InjectJunitExtension.class)
class InjectExtension_testDoubleViaMock2_Test {

  @Inject HelloService helloService;

  @Mock HelloData mockData = initMe();

  private HelloData initMe() {
    HelloData helloData = Mockito.mock(HelloData.class);
    when(helloData.helloData()).thenReturn("CreatedByInitMe");
    return helloData;
  }

  @Test
  void hello_1() {
    assertEquals("hello+CreatedByInitMe", helloService.hello());
  }

  @Test
  void hello_2() {
    when(mockData.helloData()).thenReturn("LocalMock_2");
    assertEquals("hello+LocalMock_2", helloService.hello());
  }

}
