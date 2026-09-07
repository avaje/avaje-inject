package io.avaje.inject.test;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ServiceLoader;
import io.avaje.inject.spi.AvajeModule;
import io.avaje.inject.spi.InjectExtension;

/** Resolves the {@code AvajeModule} instances for {@code @InjectTest(modules = ...)}. */
final class SelectedModules {

  private static final AvajeModule[] EMPTY = {};

  private SelectedModules() {}

  /** Return the module instances for the given module classes. */
  static AvajeModule[] instances(Class<? extends AvajeModule>[] moduleClasses) {
    final var wanted = new LinkedHashSet<>(List.of(moduleClasses));
    final List<AvajeModule> modules = new ArrayList<>(wanted.size());
    ServiceLoader.load(InjectExtension.class).stream()
        .filter(provider -> wanted.contains(provider.type()))
        .forEach(
            provider -> {
              wanted.remove(provider.type());
              modules.add((AvajeModule) provider.get());
            });
    // fall back to reflection if the module is not registered as a service.
    for (var cls : wanted) {
      modules.add(newInstance(cls));
    }
    return modules.toArray(EMPTY);
  }

  private static AvajeModule newInstance(Class<? extends AvajeModule> cls) {
    try {
      return cls.getDeclaredConstructor().newInstance();
    } catch (ReflectiveOperationException e) {
      throw new IllegalStateException(
          "Failed to create @InjectTest(modules = " + cls.getTypeName() + ")", e);
    }
  }
}
