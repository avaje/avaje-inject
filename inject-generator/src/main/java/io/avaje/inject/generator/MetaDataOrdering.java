package io.avaje.inject.generator;

import static io.avaje.inject.generator.APContext.logError;
import static io.avaje.inject.generator.APContext.logWarn;
import static io.avaje.inject.generator.ProcessingContext.elementMaybe;
import static io.avaje.inject.generator.ProcessingContext.externallyProvided;

import javax.lang.model.element.TypeElement;

import static java.util.stream.Collectors.toList;

import java.util.*;

final class MetaDataOrdering {

  private static final String CIRC_ERR_MSG =
    "To handle circular dependencies consider using field injection " +
      "rather than constructor injection on one of the dependencies. " +
      "\n See https://avaje.io/inject/#circular";

  private final ScopeInfo scopeInfo;
  private final List<MetaData> orderedList = new ArrayList<>();
  private final List<MetaData> queue = new ArrayList<>();
  private final Map<String, ProviderList> providers = new HashMap<>();
  private final List<DependencyLink> circularDependencies = new ArrayList<>();
  private final Set<String> missingDependencyTypes = new LinkedHashSet<>();
  private final Set<String> autoRequires = new TreeSet<>();
  private final Set<String> autoRequiresAspects = new TreeSet<>();

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
      final String aspect = metaData.providesAspect();
      if (aspect != null && !aspect.isEmpty()) {
        providerAdd(Util.wrapAspect(aspect)).add(metaData);
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
      count = processQueueRound(false);
    } while (count > 0);
    do {
      // run again including externally provided dependencies from other modules
      count = processQueueRound(true);
    } while (count > 0);

    int remaining = queue.size();
    if (remaining != 0) {
      missingDependencies();
      orderedList.addAll(queue);
    }
    return remaining;
  }

  /**
   * Try to detect circular dependency given the remaining beans
   * in the queue with unsatisfied dependencies.
   */
  private void detectCircularDependency(List<MetaData> remainder) {
    final List<DependencyLink> dependencyLinks = new ArrayList<>();
    for (MetaData metaData : remainder) {
      final List<Dependency> dependsOn = metaData.dependsOn();
      if (dependsOn != null) {
        for (Dependency dependency : dependsOn) {
          final MetaData provider = findCircularDependency(remainder, dependency);
          if (provider != null) {
            dependencyLinks.add(new DependencyLink(metaData, provider, dependency.name()));
          }
        }
      }
    }
    if (dependencyLinks.size() > 1) {
      // need minimum of 2 to form circular dependency
      circularDependencies.addAll(dependencyLinks);
    }
  }

  private MetaData findCircularDependency(List<MetaData> remainder, Dependency dependency) {
    for (MetaData metaData : remainder) {
      if (metaData.toString().contains(dependency.name())) {
        return metaData;
      }
      final List<String> provides = metaData.provides();
      if (provides != null && provides.contains(dependency.name())) {
        return metaData;
      }
    }
    return null;
  }

  /**
   * Log a reasonable compile error for detected circular dependencies.
   */
  private void errorOnCircularDependencies() {
    logError("Circular dependencies detected with beans %s  %s", circularDependencies, CIRC_ERR_MSG);
    for (DependencyLink link : circularDependencies) {
      logError("Circular dependency - %s dependsOn %s for %s", link.metaData, link.provider, link.dependency);
    }
  }

  /**
   * Build list of specific dependencies that are missing.
   */
  void missingDependencies() {
    for (MetaData metaData : queue) {
      checkMissingDependencies(metaData);
    }
    if (missingDependencyTypes.isEmpty()) {
      // only look for circular dependencies if there are no missing dependencies
      detectCircularDependency(queue);
    }
  }

  private void checkMissingDependencies(MetaData metaData) {
    for (Dependency dependency : metaData.dependsOn()) {
      if (providers.get(dependency.name()) == null && !scopeInfo.providedByOtherScope(dependency.name())) {
        TypeElement element = elementMaybe(metaData.type());
        logError(element, "No dependency provided for " + dependency + " on " + metaData.type());
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
      logWarn("There are " + queue.size() + " beans with unsatisfied dependencies (assuming external dependencies)");
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

  private int processQueueRound(boolean includeExternal) {
    // loop queue looking for entry that has all provides marked as included
    int count = 0;
    Iterator<MetaData> iterator = queue.iterator();
    while (iterator.hasNext()) {
      MetaData queuedMeta = iterator.next();
      if (allDependenciesWired(queuedMeta, includeExternal)) {
        orderedList.add(queuedMeta);
        queuedMeta.setWired();
        iterator.remove();
        count++;
      }
    }
    return count;
  }

  private boolean allDependenciesWired(MetaData queuedMeta, boolean includeExternal) {
    for (Dependency dependency : queuedMeta.dependsOn()) {
      String dependencyName = dependency.name();
      if (Util.isProvider(dependencyName) || Constants.BEANSCOPE.equals(dependencyName)) {
        continue;
      }
      if (!dependencySatisfied(dependency, includeExternal, queuedMeta)) {
        return false;
      }
    }
    return true;
  }

  private boolean dependencySatisfied(Dependency dependency, boolean includeExternal, MetaData queuedMeta) {
    String dependencyName = dependency.name();
    var providerList = providers.get(dependencyName);
    if (providerList == null) {
      if (scopeInfo.providedByOther(dependency)) {
        return true;
      } else {
        return isExternal(dependencyName, includeExternal, queuedMeta);
      }
    } else {
      return providerList.isAllWired();
    }
  }

  private boolean isExternal(String dependencyName, boolean includeExternal, MetaData queuedMeta) {
    if (includeExternal && externallyProvided(dependencyName)) {
      if (Util.isAspectProvider(dependencyName)) {
        autoRequiresAspects.add(Util.extractAspectType(dependencyName));
      } else {
        autoRequires.add(dependencyName);
      }
      queuedMeta.markWithExternalDependency(dependencyName);
      return true;
    }
    return false;
  }

  Set<String> autoRequires() {
    return autoRequires;
  }

  Set<String> autoRequiresAspects() {
    return autoRequiresAspects;
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
   * Return true if the beans with unsatisfied dependencies seem
   * to form a circular dependency.
   */
  private boolean hasCircularDependencies() {
    return !circularDependencies.isEmpty();
  }

  private static class ProviderList {

    private final List<MetaData> list = new ArrayList<>();

    void add(MetaData beanMeta) {
      list.add(beanMeta);
    }

    boolean isAllWired() {
      for (MetaData metaData : list) {
        if (!metaData.isWired()) {
          return false;
        }
      }
      return true;
    }
  }

  private static class DependencyLink {

    final MetaData metaData;
    final MetaData provider;
    final String dependency;

    DependencyLink(MetaData metaData, MetaData provider, String dependency) {
      this.metaData = metaData;
      this.provider = provider;
      this.dependency = dependency;
    }

    @Override
    public String toString() {
      return metaData.toString();
    }
  }
}
