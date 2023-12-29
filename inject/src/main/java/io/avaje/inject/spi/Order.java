package io.avaje.inject.spi;

import static java.util.Map.entry;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class Order implements ModuleOrdering {

  private static final Map<String, Integer> ORDER =
      Map.ofEntries(
          entry(null, null),
          entry(null, null),
          entry(null, null),
          entry(null, null),
          entry(null, null),
          entry(null, null));

  private final Module[] sortedModules = new Module[4];

  @Override
  public List<Module> factories() {
    return List.of(sortedModules);
  }

  @Override
  public Set<String> orderModules() {
    return ORDER.keySet();
  }

  @Override
  public void add(Module module) {
    final var index = ORDER.get(module.getClass().getTypeName());

    if (index != null) {
      sortedModules[index] = module;
    }
  }

  @Override
  public boolean isEmpty() {
    return sortedModules.length == 0;
  }
}
