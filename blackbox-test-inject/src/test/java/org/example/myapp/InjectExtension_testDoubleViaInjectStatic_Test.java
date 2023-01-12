package org.example.myapp;

import io.avaje.inject.test.InjectTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@InjectTest
class InjectExtension_testDoubleViaInjectStatic_Test {

  static @Inject HelloService helloService;

  static @Inject HelloData myTestDouble = () -> "ImActuallyATestDouble";

  @Test
  void hello_1() {
    assertEquals("hello+ImActuallyATestDouble", helloService.hello());
    assertEquals("ImActuallyATestDouble", myTestDouble.helloData());
  }

}
