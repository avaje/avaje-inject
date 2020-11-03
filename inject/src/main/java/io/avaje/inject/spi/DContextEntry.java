package io.avaje.inject.spi;

import io.avaje.inject.BeanEntry;

import java.util.ArrayList;
import java.util.List;

/**
 * Entry for a given key (bean class, interface class or annotation class).
 * <p>
 * This holds a list of managed beans (which might be named).
 */
class DContextEntry {

  private final List<DContextEntryBean> entries = new ArrayList<>();

  @SuppressWarnings("unchecked")
  <T> BeanEntry<T> candidate(String name) {
    if (entries.isEmpty()) {
      return null;
    }
    if (entries.size() == 1) {
      DContextEntryBean entry = entries.get(0);
      return entry.candidate(name);
    }

    EntryMatcher matcher = new EntryMatcher(name);
    matcher.match(entries);
    return matcher.candidate();
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

  /**
   * Return true if a supplied bean is one of the entries.
   */
  boolean isSupplied() {
    for (DContextEntryBean entry : entries) {
      if (entry.isSupplied()) {
        return true;
      }
    }
    return false;
  }

  static class EntryMatcher {

    private final String name;

    private DContextEntryBean match;
    private DContextEntryBean ignoredSecondaryMatch;

    EntryMatcher(String name) {
      this.name = name;
    }

    void match(List<DContextEntryBean> entries) {
      for (DContextEntryBean entry : entries) {
        if (entry.isNameMatch(name)) {
          checkMatch(entry);
        }
      }
    }

    private void checkMatch(DContextEntryBean entry) {
      if (match == null) {
        match = entry;
        return;
      }
      if (match.isSecondary() && !entry.isSecondary()) {
        // secondary loses
        match = entry;
        return;
      }
      if (match.isPrimary()) {
        if (entry.isPrimary()) {
          throw new IllegalStateException("Expecting only 1 bean match but have multiple primary beans " + match.getBean() + " and " + entry.getBean());
        }
        // leave as is, current primary wins
        return;
      }
      if (entry.isSecondary()) {
        if (match.isSecondary()) {
          ignoredSecondaryMatch = entry;
        }
        return;
      }
      if (entry.isPrimary()) {
        // new primary wins
        match = entry;
        return;
      }
      throw new IllegalStateException("Expecting only 1 bean match but have multiple matching beans " + match.getBean() + " and " + entry.getBean());
    }

    @SuppressWarnings("rawtypes")
    BeanEntry candidate() {
      if (match == null) {
        return null;
      }
      checkSecondary();
      return match.getBeanEntry();
    }

    private void checkSecondary() {
      if (match.isSecondary() && ignoredSecondaryMatch != null) {
        throw new IllegalStateException("Expecting only 1 bean match but have multiple secondary beans " + match.getBean() + " and " + ignoredSecondaryMatch.getBean());
      }
    }

  }
}
