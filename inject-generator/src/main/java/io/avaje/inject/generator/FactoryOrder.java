package io.avaje.inject.generator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Helper to order the BeanContextFactory based on dependsOn. */
class FactoryOrder {

  private final Set<String> moduleNames = new LinkedHashSet<>();
  private final List<AvajeModule> factories = new ArrayList<>();
  private final List<FactoryState> queue = new ArrayList<>();
  private final List<FactoryState> queueNoDependencies = new ArrayList<>();
  private final Map<String, List<String>> unsatisfiedDependencies = new HashMap<>();

  private final Map<String, FactoryList> providesMap = new HashMap<>();
  private final Set<String> pluginProvided;
  private final List<String> loadedModules = new ArrayList<>();

  FactoryOrder(Collection<AvajeModule> includeModules, Set<String> pluginProvided) {
    includeModules.forEach(m -> {
      add(m);
      loadedModules.add(m.name());
    });
    this.pluginProvided = pluginProvided;
  }

  void add(AvajeModule module) {
    final FactoryState factoryState = new FactoryState(module);
    providesMap.computeIfAbsent(module.name(), s -> new FactoryList()).add(factoryState);
    addFactoryProvides(factoryState, module.provides());

    if (factoryState.isRequiresEmpty()) {
      if (factoryState.explicitlyProvides()) {
        // push immediately when explicitly 'provides' with no 'requires'
        push(factoryState);
      } else {
        // hold until after all the explicitly 'provides' modules are added
        queueNoDependencies.add(factoryState);
      }
    } else {
      // queue it to process by dependency ordering
      queue.add(factoryState);
    }
  }

  private void addFactoryProvides(FactoryState factoryState, List<String> list) {
    for (final var feature : list) {
      providesMap.computeIfAbsent(feature, s -> new FactoryList()).add(factoryState);
    }
  }

  /** Push the factory onto the build order (the wiring order for modules). */
  private void push(FactoryState factory) {
    factory.setPushed();
    factories.add(factory.factory());
    moduleNames.add(factory.factory().name());
  }

  Set<String> orderModules() {
    // push the 'no dependency' modules after the 'provides only' ones
    // as this is more intuitive for the simple (only provides modules case)
    for (final FactoryState factoryState : queueNoDependencies) {
      push(factoryState);
    }
    processQueue();
    return moduleNames;
  }

  /** Process the queue pushing the factories in order to satisfy dependencies. */
  private void processQueue() {
    int count;
    do {
      count = processQueuedFactories();
    } while (count > 0);

    if (!queue.isEmpty()) {
      final StringBuilder sb = new StringBuilder();
      for (final FactoryState factory : queue) {
        sb.append("Module [").append(factory).append("] has unsatisfied");
        unsatisfiedRequires(sb, factory.factory);
      }
      sb.append(" - none of the loaded modules ")
        .append(loadedModules)
        .append(" explicitly provide the dependencies.");
      if (ProcessingContext.strictWiring()) {
        APContext.logError(sb.toString());
      } else {
        APContext.logNote(sb.toString());
      }
    }
  }

  private void unsatisfiedRequires(StringBuilder sb, AvajeModule module) {
    for (final var depModuleName : module.requires()) {
      if (notProvided(depModuleName)) {
        unsatisfiedDependencies
          .computeIfAbsent(module.name(), k -> new ArrayList<>())
          .add(depModuleName);
        sb.append(String.format(" requires [%s]", depModuleName));
      }
    }
  }

  private boolean notProvided(String dependency) {
    final FactoryList factoryList = providesMap.get(dependency);
    return (factoryList == null || !factoryList.allPushed())
        && !pluginProvided.contains(dependency)
        && !ProcessingContext.externallyProvided(dependency);
  }

  /**
   * Process the queued factories pushing them when all their (module) dependencies are satisfied.
   *
   * <p>This returns the number of factories added so once this returns 0 it is done.
   */
  private int processQueuedFactories() {
    int count = 0;
    final Iterator<FactoryState> it = queue.iterator();
    while (it.hasNext()) {
      final FactoryState factory = it.next();
      if (satisfiedDependencies(factory.requires())) {
        // push the factory onto the build order
        it.remove();
        push(factory);
        count++;
      }
    }
    return count;
  }

  /** Return true if the (module) requires dependencies are satisfied for this factory. */
  private boolean satisfiedDependencies(List<String> requires) {
    for (final var dependency : requires) {
      if (notProvided(dependency)) {
        return false;
      }
    }
    return true;
  }

  boolean isEmpty() {
    return factories.isEmpty();
  }

  /** List of factories for a given name or feature. */
  static class FactoryList {

    private final List<FactoryState> factories = new ArrayList<>();

    void add(FactoryState factory) {
      factories.add(factory);
    }

    /** Return true if all factories here have been pushed onto the build order. */
    boolean allPushed() {
      for (final FactoryState factory : factories) {
        if (!factory.isPushed()) {
          return false;
        }
      }
      return true;
    }
  }

  /** Wrapper on Factory holding the pushed state. */
  static class FactoryState {

    private final AvajeModule factory;
    private boolean pushed;

    FactoryState(AvajeModule factory) {
      this.factory = factory;
    }

    /** Set when factory is pushed onto the build/wiring order. */
    void setPushed() {
      this.pushed = true;
    }

    boolean isPushed() {
      return pushed;
    }

    AvajeModule factory() {
      return factory;
    }

    List<String> requires() {
      return factory.requires();
    }

    @Override
    public String toString() {
      return factory.getClass().getTypeName();
    }

    boolean isRequiresEmpty() {
      return factory.requires().isEmpty();
    }

    boolean explicitlyProvides() {
      return !factory.provides().isEmpty();
    }
  }

  Map<String, List<String>> unsatisfied() {
    return unsatisfiedDependencies;
  }
}
