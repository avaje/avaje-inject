package io.dinject.core;

import java.util.ArrayList;
import java.util.List;

/**
 * Entry for a given key (bean class, interface class or annotation class).
 *
 * This holds a list of managed beans (which might be named).
 */
class DContextEntry {

  private final List<DContextEntryBean> entries = new ArrayList<>();

  /**
   * Get a single bean given the name.
   */
  Object get(String name) {
    if (entries.isEmpty()) {
      return null;
    }
    if (entries.size() == 1) {
      DContextEntryBean entry = entries.get(0);
      return entry.getIfMatchWithDefault(name);
    }
    List<Object> beans = allMatches(name);
    if (beans.isEmpty()) {
      return null;
    }
    if (beans.size() == 1) {
      return beans.get(0);
    } else {
      throw new IllegalStateException("Expecting only 1 bean match but found " + beans.size());
    }
  }

  /**
   * Return all the beans that match on the given name.
   */
  List<Object> allMatches(String name) {
    List<Object> beans = new ArrayList<>();
    for (DContextEntryBean entry : entries) {
      if (entry.isNameMatch(name)) {
        beans.add(entry.getBean());
      }
    }

    return beans;
  }

  /**
   * Add all the managed beans to the given list.
   */
  void addAll(List<Object> appendToList) {
    for (DContextEntryBean entry : entries) {
      appendToList.add(entry.getBean());
    }
  }

  void add(DContextEntryBean entryBean) {
    entries.add(entryBean);
  }

}
