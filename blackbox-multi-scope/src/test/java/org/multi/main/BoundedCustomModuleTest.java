package org.multi.main;

import io.avaje.inject.test.InjectTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.multi.parents.BeanIn1;
import org.multi.parents.Mod1Module;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Bound the wiring to a custom scope module. Custom scope modules are not registered as an {@code
 * InjectExtension} service, so this covers the reflection fallback in {@code SelectedModules}.
 */
@InjectTest(modules = Mod1Module.class)
class BoundedCustomModuleTest {

  static int constructedBefore;

  @Inject BeanIn1 beanIn1;

  @Test
  void onlyTheSelectedModuleIsWired() {
    assertNotNull(beanIn1);
  }
}
