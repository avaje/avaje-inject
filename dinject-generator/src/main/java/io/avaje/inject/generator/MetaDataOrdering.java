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

  private final ProcessingContext processingContext;

  private final List<MetaData> orderedList = new ArrayList<>();

  private final List<MetaData> queue = new ArrayList<>();

  private final Map<String, ProviderList> providers = new HashMap<>();

  private String topPackage;

  MetaDataOrdering(Collection<MetaData> values, ProcessingContext processingContext) {
    this.processingContext = processingContext;

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
      orderedList.addAll(queue);
    }
    return remaining;
  }

  void warnOnDependencies() {
    for (MetaData m : queue) {
      processingContext.logWarn("unsatisfied dependencies on %s dependsOn %s", m.getType(), m.getDependsOn());
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

  private static class ProviderList {

    private List<MetaData> list = new ArrayList<>();

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
}
