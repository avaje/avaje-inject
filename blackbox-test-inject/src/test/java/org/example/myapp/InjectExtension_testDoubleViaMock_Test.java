package org.example.myapp;

import io.avaje.inject.test.InjectExtension;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(InjectJunitExtension.class)
class InjectExtension_testDoubleViaMock_Test {

  @Inject HelloService helloService;

  @Mock HelloData mockData = () -> "ImAConcreteTestDouble";

  @Test
  void hello_1() {
    assertEquals("hello+ImAConcreteTestDouble", helloService.hello());
    assertEquals("ImAConcreteTestDouble", mockData.helloData());
  }

}
