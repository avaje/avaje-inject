package org.example.myapp;

import io.avaje.inject.test.InjectExtension;
import jakarta.inject.Inject;
import org.example.myapp.testconfig.CountTestScopeStart;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(InjectJunitExtension.class)
class InjectExtension_injectFromTestModule_Test {

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

  @Test
  void globalTestBeanScope_expect_startedOnceOnly() {
    assertEquals(1, CountTestScopeStart.started.get());
  }

  @Test
  void globalTestBeanScope_expect_startedOnceOnly_again() {
    assertEquals(1, CountTestScopeStart.started.get());
  }
}
