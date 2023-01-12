package org.example.myapp;

import io.avaje.inject.test.InjectExtension;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(InjectExtension.class)
class InjectExtension_testDoubleViaInject_Test {

  @Inject HelloService helloService;

  @Inject HelloData myTestDouble = () -> "ImActuallyATestDouble";

  @Test
  void hello_1() {
    assertEquals("hello+ImActuallyATestDouble", helloService.hello());
    assertEquals("ImActuallyATestDouble", myTestDouble.helloData());
  }

}
