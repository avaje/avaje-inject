package io.avaje.inject.test;

import io.avaje.inject.spi.AvajeModule;

/** Marker for Test scope module. */
public interface TestModule extends AvajeModule.Custom {

  /**
   * Return the modules that tests wire by default, as specified by {@link TestModules}.
   *
   * <p>When empty, tests wire every {@code AvajeModule} found on the classpath.
   */
  default Class<? extends AvajeModule>[] testModules() {
    return SelectedModules.NONE;
  }
}
