package org.example.myapp.modules;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.example.external.aspect.sub.ExampleExternalAspectModule;
import org.example.myapp.HelloData;
import org.example.myapp.HelloService;
import org.junit.jupiter.api.Test;
import org.other.one.OtherComponent;

import io.avaje.inject.aop.MethodInterceptor;
import io.avaje.inject.test.InjectTest;
import jakarta.inject.Inject;

/**
 * Bound the wiring to a single module. {@code ExampleExternalAspectModule} is registered as an
 * {@code InjectExtension} service so this covers the ServiceLoader path in {@code SelectedModules}.
 */
@InjectTest(modules = ExampleExternalAspectModule.class)
class BoundedModulesTest {

  /** Provided by the selected module. */
  @Inject MethodInterceptor interceptor;

  /** Provided by the global test scope (parent), so still available. */
  @Inject HelloData helloData;

  /** Provided by MyappModule, which was not selected. */
  @Inject Optional<HelloService> helloService;

  /** Provided by OneModule (blackbox-other), which was not selected. */
  @Inject Optional<OtherComponent> otherComponent;

  @Test
  void selectedModuleIsWired() {
    assertThat(interceptor).isNotNull();
  }

  @Test
  void parentTestScopeIsStillInherited() {
    assertThat(helloData.helloData()).isEqualTo("TestHelloData");
  }

  @Test
  void unselectedModulesAreNotWired() {
    assertThat(helloService).isEmpty();
    assertThat(otherComponent).isEmpty();
  }
}
