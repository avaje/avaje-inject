package io.avaje.inject.generator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

class MetaDataOrdering {

  private static final String CIRC_ERR_MSG =
    "To handle circular dependencies consider using field injection " +
      "rather than constructor injection on one of the dependencies. " +
      "\n See https://avaje.io/inject/#circular";

  private final ProcessingContext context;

  private final List<MetaData> orderedList = new ArrayList<>();

  private final List<MetaData> queue = new ArrayList<>();

  private final Map<String, ProviderList> providers = new HashMap<>();

  private final List<DependencyLink> circularDependencies = new ArrayList<>();

  private String topPackage;

  MetaDataOrdering(Collection<MetaData> values, ProcessingContext context) {
    this.context = context;

    for (MetaData metaData : values) {
      if (metaData.noDepends()) {
        orderedList.add(metaData);
        metaData.setWired();
      } else {
        queue.add(metaData);
      }
      topPackage = Util.commonParent(topPackage, metaData.getTopPackage());

      // register into map keyed by provider
      providers.computeIfAbsent(metaData.getType(), s -> new ProviderList()).add(metaData);
      for (String provide : metaData.getProvides()) {
        providers.computeIfAbsent(provide, s -> new ProviderList()).add(metaData);
      }
    }

    // order no dependency list by ... name asc (order does not matter but for consistency)
  }

  int processQueue() {

    int count;
    do {
      count = processQueueRound();
    } while (count > 0);

    int remaining = queue.size();
    if (remaining != 0) {
      detectCircularDependency(queue);
      orderedList.addAll(queue);
    }
    return remaining;
  }

  /**
   * Try to detect circular dependency given the remaining beans
   * in the queue with unsatisfied dependencies.
   */
  private void detectCircularDependency(List<MetaData> remainder) {
    for (MetaData metaData : remainder) {
      final List<String> dependsOn = metaData.getDependsOn();
      if (dependsOn != null) {
        for (String dependency : dependsOn) {
          final MetaData provider = findCircularDependency(remainder, dependency);
          if (provider != null) {
            circularDependencies.add(new DependencyLink(metaData, provider, dependency));
          }
        }
      }
    }
  }

  private MetaData findCircularDependency(List<MetaData> remainder, String dependency) {
    for (MetaData metaData : remainder) {
      if (metaData.getType().equals(dependency)) {
        return metaData;
      }
      final List<String> provides = metaData.getProvides();
      if (provides != null && provides.contains(dependency)) {
        return metaData;
      }
    }
    return null;
  }

  /**
   * Log a reasonable compile error for detected circular dependencies.
   */
  void errorOnCircularDependencies() {
    context.logError("Circular dependencies detected with beans %s  %s", circularDependencies, CIRC_ERR_MSG);
    for (DependencyLink link : circularDependencies) {
      context.logError("Circular dependency - %s dependsOn %s for %s", link.metaData, link.provider, link.dependency);
    }
  }

  /**
   * Log a warning on unsatisfied dependencies that are expected to be
   * provided by another module.
   */
  void warnOnDependencies() {
    for (MetaData m : queue) {
      context.logWarn("Unsatisfied dependencies on %s dependsOn %s", m, m.getDependsOn());
    }
  }

  String getTopPackage() {
    return topPackage;
  }

  private int processQueueRound() {

    // loop queue looking for entry that has all provides marked as included

    int count = 0;

    Iterator<MetaData> iterator = queue.iterator();
    while (iterator.hasNext()) {
      MetaData queuedMeta = iterator.next();
      if (allDependenciesWired(queuedMeta)) {
        orderedList.add(queuedMeta);
        queuedMeta.setWired();
        iterator.remove();
        count++;
      }
    }

    return count;
  }

  private boolean allDependenciesWired(MetaData queuedMeta) {

    for (String dependency : queuedMeta.getDependsOn()) {
      ProviderList providerList = providers.get(dependency);
      if (providerList == null) {
        // missing dependencies - leave to end
        return false;
      } else {
        if (!providerList.isAllWired()) {
          return false;
        }
      }
    }
    return true;
  }

  List<MetaData> getOrdered() {
    return orderedList;
  }

  Set<String> getImportTypes() {
    Set<String> importTypes = new TreeSet<>();
    for (MetaData metaData : orderedList) {
      metaData.addImportTypes(importTypes);
    }
    return importTypes;
  }

  /**
   * Return the MetaData for the bean that provides the (generic interface) dependency.
   */
  MetaData findProviderOf(String depend) {

    for (MetaData metaData : orderedList) {
      List<String> provides = metaData.getProvides();
      if (provides != null) {
        for (String provide : provides) {
          if (provide.equals(depend)) {
            return metaData;
          }
        }
      }
    }
    return null;
  }

  /**
   * Return true if the beans with unsatisfied dependencies seem
   * to form a circular dependency.
   */
  boolean hasCircularDependencies() {
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
