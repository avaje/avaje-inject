package org.multi.globalscope;

import io.avaje.inject.test.InjectTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Bound the wiring to the local module only - OneModule is on the test classpath but is not
 * wired at all.
 *
 * <p>GlobalscopeModule is registered as an {@code InjectExtension} service so this covers the
 * ServiceLoader lookup in {@code SelectedModules}.
 */
@InjectTest(modules = GlobalscopeModule.class)
class T6BoundedModulesTest {

  @Inject RegistryBean bean;
  @Inject Greeter greeter;

  @Test
  void onlyTheSelectedModuleIsWired() {
    assertNotNull(bean.lookup());
    assertEquals("hello", greeter.greet());
  }
}
