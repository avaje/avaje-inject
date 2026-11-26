package org.example.myapp.modules;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.example.external.aspect.sub.ExampleExternalAspectModule;
import org.example.myapp.HelloService;
import org.junit.jupiter.api.Test;

import io.avaje.inject.aop.MethodInterceptor;
import io.avaje.inject.test.InjectTest;
import jakarta.inject.Inject;

/**
 * The selected modules must also bound the class level BeanScope, which is built without a test
 * instance - so {@code MetaInfo} has to read them from the test class.
 */
@InjectTest(modules = ExampleExternalAspectModule.class)
class BoundedModulesStaticTest {

  @Inject static MethodInterceptor interceptor;

  @Inject static Optional<HelloService> helloService;

  @Test
  void classLevelScopeIsAlsoBounded() {
    assertThat(interceptor).isNotNull();
    assertThat(helloService).isEmpty();
  }
}
