package io.avaje.inject.spi;

import java.util.ArrayList;
import java.util.List;

/**
 * Entry for a given key (bean class, interface class or annotation class).
 * <p>
 * This holds a list of managed beans (which might be named).
 */
class DContextEntry {

  private final List<DContextEntryBean> entries = new ArrayList<>(5);

  List<DContextEntryBean> entries() {
    return entries;
  }

  void add(DContextEntryBean entryBean) {
    entries.add(entryBean);
  }

  Object get(String name) {
    if (entries.size() == 1) {
      return entries.get(0).getBean();
    }
    return new EntryMatcher(name).match(entries);
  }

  /**
   * Return all the beans.
   */
  List<Object> all() {
    List<Object> list = new ArrayList<>(entries.size());
    for (DContextEntryBean entry : entries) {
      list.add(entry.getBean());
    }
    return list;
  }

  /**
   * Return true if a supplied bean is one of the entries.
   */
  boolean isSupplied(String qualifierName) {
    for (DContextEntryBean entry : entries) {
      if (entry.isSupplied(qualifierName)) {
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

    Object match(List<DContextEntryBean> entries) {
      for (DContextEntryBean entry : entries) {
        if (entry.isNameMatch(name)) {
          checkMatch(entry);
        }
      }
      return candidate();
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
      // try to resolve match using qualifier name (including null)
      if (match.isNameEqual(name) && !entry.isNameEqual(name)) {
        ignoredSecondaryMatch = entry;
        return;
      } else if (!match.isNameEqual(name) && entry.isNameEqual(name)) {
        match = entry;
        return;
      }
      throw new IllegalStateException("Expecting only 1 bean match but have multiple matching beans " + match.getBean()
        + " and " + entry.getBean() + ". Maybe need a rebuild is required after adding a @Named qualifier?");
    }

    private Object candidate() {
      if (match == null) {
        return null;
      }
      checkSecondary();
      return match.getBean();
    }

    private void checkSecondary() {
      if (match.isSecondary() && ignoredSecondaryMatch != null) {
        throw new IllegalStateException("Expecting only 1 bean match but have multiple secondary beans " + match.getBean() + " and " + ignoredSecondaryMatch.getBean());
      }
    }

  }
}
