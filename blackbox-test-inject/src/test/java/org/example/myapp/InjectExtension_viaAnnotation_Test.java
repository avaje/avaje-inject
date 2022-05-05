package org.example.myapp;

import io.avaje.inject.test.InjectTest;
import javax.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@InjectTest
class InjectExtension_viaAnnotation_Test {

  @Inject
  HelloService helloService;

  @Test
  void hello_1() {
    assertEquals("hello+TestHelloData", helloService.hello());
  }

  @Test
  void hello_2() {
    assertEquals("hello+TestHelloData", helloService.hello());
  }

}
