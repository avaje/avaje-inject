package io.avaje.inject.generator;

import static io.avaje.inject.generator.APContext.logError;
import static io.avaje.inject.generator.APContext.logWarn;
import static io.avaje.inject.generator.ProcessingContext.elementMaybe;
import static io.avaje.inject.generator.ProcessingContext.externallyProvided;

import javax.lang.model.element.TypeElement;

import static java.util.stream.Collectors.toList;

import java.util.*;
import java.util.stream.Collectors;

final class MetaDataOrdering {

  private static final String CIRC_ERR_MSG =
    "To handle circular dependencies consider using field injection " +
      "rather than constructor injection on one of the dependencies. " +
      "\n See https://avaje.io/inject/#circular";

  private final ScopeInfo scopeInfo;
  private final List<MetaData> orderedList = new ArrayList<>();
  private final List<MetaData> queue = new ArrayList<>();
  private final Map<String, ProviderList> providers = new HashMap<>();
  private final List<List<MetaData>> cyclePaths = new ArrayList<>();
  private final Set<String> missingDependencyTypes = new LinkedHashSet<>();
  private final Set<String> autoRequires = new TreeSet<>();

  MetaDataOrdering(Collection<MetaData> values, ScopeInfo scopeInfo) {
    this.scopeInfo = scopeInfo;
    for (MetaData metaData : values) {
      if (metaData.noDepends()) {
        orderedList.add(metaData);
        metaData.setWired();
      } else {
        queue.add(metaData);
      }
      // register into map keyed by provider
      providerAdd(metaData.toString()).add(metaData);
      providerAdd(metaData.type()).add(metaData);
      for (String provide : metaData.provides()) {
        providerAdd(provide).add(metaData);
      }
    }
    externallyRequiredDependencies();
  }

  /**
   * These, if defined are expected to be required at wiring time probably via another module.
   */
  private void externallyRequiredDependencies() {
    for (String requireType : scopeInfo.requires()) {
      providerAdd(requireType);
    }
    for (String requireType : scopeInfo.pluginProvided()) {
      providerAdd(requireType);
    }
  }

  private ProviderList providerAdd(String requireType) {
    return providers.computeIfAbsent(requireType.replace(", ", ","), s -> new ProviderList());
  }

  int processQueue() {
    int count;
    do {
      // first run without external dependencies from other modules
      count = processQueueRound(false, false);
    } while (count > 0);
    do {
      // run again including externally provided dependencies from other modules
      count = processQueueRound(true, false);
    } while (count > 0);

    do {
      // Last ditch effort, match any bean available
      count = processQueueRound(true, true);
    } while (count > 0);

    int remaining = queue.size();
    if (remaining != 0) {
      missingDependencies();
      orderedList.addAll(queue);
    }
    return remaining;
  }

  /**
   * Build list of specific dependencies that are missing. Runs cycle detection first so that a
   * clear circular-dependency error is shown instead of misleading "No dependency provided" errors
   * for each bean in the cycle.
   */
  void missingDependencies() {
    detectCircularDependency(queue);
    if (!cyclePaths.isEmpty()) {
      return;
    }
    for (MetaData metaData : queue) {
      checkMissingDependencies(metaData);
    }
  }

  private void checkMissingDependencies(MetaData metaData) {
    for (Dependency dependency : metaData.dependsOn()) {
      if (!dependencySatisfied(dependency, true, metaData, true)) {
        TypeElement element = elementMaybe(metaData.type());
        logError(element, "No dependency provided for %s on %s", dependency, metaData.type());
        missingDependencyTypes.add(dependency.name());
      }
    }
  }

  /**
   * Log a warning on unsatisfied dependencies that are expected to be provided by another module.
   */
  private void warnOnDependencies() {
    if (!missingDependencyTypes.isEmpty()) {
      var missingMessage = missingDependencyTypes.stream()
        .map(s -> s.replaceFirst(":", " with qualifier: "))
        .collect(toList());
      logError("Dependencies %s are not provided - there are no @Singleton, @Component, @Factory/@Bean that currently provide this type. If this is an external dependency consider specifying via @External", missingMessage);
    } else if (!queue.isEmpty()) {
      logWarn("There are %s beans with unsatisfied dependencies (assuming external dependencies)", queue.size());
      for (MetaData m : queue) {
        logWarn("Unsatisfied dependencies on %s dependsOn %s", m, m.dependsOn());
      }
    }
  }

  void logWarnings() {
    if (hasCircularDependencies()) {
      errorOnCircularDependencies();
    } else {
      warnOnDependencies();
    }
  }

  private int processQueueRound(boolean includeExternal, boolean anyWired) {
    // loop queue looking for entry that has all provides marked as included
    int count = 0;
    Iterator<MetaData> iterator = queue.iterator();
    while (iterator.hasNext()) {
      MetaData queuedMeta = iterator.next();
      if (allDependenciesWired(queuedMeta, includeExternal, anyWired)) {
        orderedList.add(queuedMeta);
        queuedMeta.setWired();
        iterator.remove();
        count++;
      }
    }
    return count;
  }

  private boolean allDependenciesWired(MetaData queuedMeta, boolean includeExternal, boolean anyWired) {
    for (Dependency dependency : queuedMeta.dependsOn()) {
      String dependencyName = dependency.name();
      if (Util.isProvider(dependencyName) || Constants.BEANSCOPE.equals(dependencyName)) {
        continue;
      }
      if (!dependencySatisfied(dependency, includeExternal, queuedMeta, anyWired)) {
        return false;
      }
    }
    return true;
  }

  private boolean dependencySatisfied(Dependency dependency, boolean includeExternal, MetaData queuedMeta, boolean anyWired) {
    String dependencyName = dependency.name();
    var providerList = providers.get(dependencyName);
    if (providerList != null) {
      return providerList.isWired(anyWired);
    }
    if (scopeInfo.providedByOther(dependency)) {
      return true;
    }
    return isExternal(dependencyName, includeExternal, queuedMeta);
  }

  private boolean isExternal(String dependencyName, boolean includeExternal, MetaData queuedMeta) {
    if (includeExternal && externallyProvided(dependencyName)) {
      autoRequires.add(dependencyName);
      queuedMeta.markWithExternalDependency(dependencyName);
      return true;
    }
    return false;
  }

  Set<String> autoRequires() {
    return autoRequires;
  }

  List<MetaData> ordered() {
    return orderedList;
  }

  Set<String> importTypes() {
    Set<String> importTypes = new TreeSet<>();
    for (MetaData metaData : orderedList) {
      metaData.addImportTypes(importTypes);
    }
    return importTypes;
  }

  /**
   * Return true if a circular dependency was detected among the remaining beans.
   */
  private boolean hasCircularDependencies() {
    return !cyclePaths.isEmpty();
  }


  /**
   * Try to detect circular dependency given the remaining beans in the queue. Uses DFS to find
   * cycles, including those routed through interface implementations or factory-produced beans.
   */
  private void detectCircularDependency(List<MetaData> remainder) {
    if (remainder.isEmpty()) return;
    final Set<MetaData> remainderSet = new HashSet<>(remainder);
    // Build adjacency: each bean -> the beans it depends on that are also in the remainder
    final Map<MetaData, List<MetaData>> graph = new LinkedHashMap<>();
    for (MetaData bean : remainder) {
      graph.put(bean, resolveQueuedDeps(bean, remainderSet));
    }
    final Set<MetaData> visited = new HashSet<>();
    final LinkedHashSet<MetaData> inStack = new LinkedHashSet<>();
    for (MetaData bean : remainder) {
      if (!visited.contains(bean)) {
        dfsCycle(bean, graph, visited, inStack);
      }
    }
  }

  /** Resolve which beans (from the remainder) a given bean depends on. */
  private List<MetaData> resolveQueuedDeps(MetaData bean, Set<MetaData> remainderSet) {
    if (bean.dependsOn() == null) return Collections.emptyList();
    final List<MetaData> result = new ArrayList<>();
    for (Dependency dep : bean.dependsOn()) {
      String depName = dep.name();
      if (Util.isProvider(depName)) {
        // Unwrap Provider<T> -> T so we can detect cycles through Provider injection
        depName = Util.unwrapProvider(depName);
      }
      if (Constants.BEANSCOPE.equals(depName)) continue;
      final ProviderList pl = providers.get(depName.replace(", ", ","));
      if (pl != null) {
        for (MetaData provider : pl.all()) {
          if (remainderSet.contains(provider)) {
            result.add(provider);
          }
        }
      }
    }
    return result;
  }

  private void dfsCycle(
      MetaData current,
      Map<MetaData, List<MetaData>> graph,
      Set<MetaData> visited,
      LinkedHashSet<MetaData> stack) {
    visited.add(current);
    stack.add(current);
    for (var neighbor : graph.getOrDefault(current, List.of())) {
      if (!visited.contains(neighbor)) {
        dfsCycle(neighbor, graph, visited, stack);
      } else if (stack.contains(neighbor)) {
        final var cycle = new ArrayList<MetaData>();
        boolean collecting = false;
        for (MetaData m : stack) {
          if (m == neighbor) collecting = true;
          if (collecting) cycle.add(m);
        }
        if (!isDuplicateCycle(cycle)) {
          cyclePaths.add(cycle);
        }
      }
    }
    stack.remove(current);
  }

  private boolean isDuplicateCycle(List<MetaData> candidate) {
    for (var existing : cyclePaths) {
      if (existing.size() == candidate.size() && existing.containsAll(candidate)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Log a clear compile error for each detected circular dependency, showing the full cycle path.
   */
  private void errorOnCircularDependencies() {
    for (var cycle : cyclePaths) {
      final var path = cycle.stream().map(MetaData::type).collect(Collectors.joining(" -> "));
      logError(
          "Circular dependency detected: %s -> %s (cycle)  %s",
          path, cycle.get(0).type(), CIRC_ERR_MSG);
    }
  }

  private static class ProviderList {

    private final List<MetaData> list = new ArrayList<>();

    private void add(MetaData beanMeta) {
      list.add(beanMeta);
    }

    private List<MetaData> all() {
      return list;
    }

    private boolean isWired(boolean anyWired) {
      return anyWired ? isAnyWired() : isAllWired();
    }

    private boolean isAllWired() {
      for (MetaData metaData : list) {
        if (!metaData.isWired()) {
          return false;
        }
      }
      return true;
    }

    private boolean isAnyWired() {
      for (MetaData metaData : list) {
        if (metaData.isWired()) {
          return true;
        }
      }
      return list.isEmpty();
    }
  }
}
