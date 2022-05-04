package org.example.myapp;

import io.avaje.inject.test.TestModule;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.ServiceLoader;

import static org.assertj.core.api.Assertions.assertThat;

class TestModule_serviceLoad_Test {

  @Test
  void test_underlying_mechanics() {
    // service load our special TestModule (everything with @TestScope)
    Optional<TestModule> testMod = ServiceLoader.load(TestModule.class).findFirst();
    assertThat(testMod).isPresent();

//    if (testMod.isPresent()) {
//      // build what is our "global test BeanScope" which we use as a parent for all tests
//      BeanScope parent = BeanScope.builder()
//        .modules(testMod.get())
//        .build();
//
//      // a test creates a BeanScope with the parent of our "global test BeanScope"
//      BeanScope child = BeanScope.builder().parent(parent, false).build();
//
//      HelloService helloService = child.get(HelloService.class);
//      assertEquals("hello+TestHelloData", helloService.hello());
//    }

  }
}
