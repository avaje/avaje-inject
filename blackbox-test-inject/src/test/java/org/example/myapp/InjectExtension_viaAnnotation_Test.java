package org.example.myapp;

import io.avaje.inject.test.InjectTest;
import javax.inject.Inject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@InjectTest
class InjectExtension_viaAnnotation_Test {

  @Inject HelloService helloService;
  static @Inject OtherService otherService;

  static OtherService _tempOtherService;

  @BeforeAll
  static void beforeAll() {
    assertThat(otherService).isNotNull();
    _tempOtherService = otherService;
  }

  @BeforeEach
  void beforeEach() {
    assertThat(helloService).isNotNull();
    assertThat(otherService).isSameAs(_tempOtherService);
  }

  @Test
  void hello_1() {
    assertEquals("hello+TestHelloData", helloService.hello());
  }

  @Test
  void hello_2() {
    assertEquals("hello+TestHelloData", helloService.hello());
  }

}
