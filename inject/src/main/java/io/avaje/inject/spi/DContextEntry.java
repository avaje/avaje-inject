package io.avaje.inject.spi;

import jakarta.inject.Provider;

import java.util.*;

/**
 * Entry for a given key (bean class, interface class or annotation class).
 * <p>
 * This holds a list of managed beans (which might be named).
 */
final class DContextEntry {

  private final List<DContextEntryBean> entries = Collections.synchronizedList(new ArrayList<>(5));

  @Override
  public String toString() {
    return String.valueOf(entries);
  }

  List<DContextEntryBean> entries() {
    return entries;
  }

  void add(DContextEntryBean entryBean) {
    entries.add(entryBean);
  }

  Provider<?> provider(String name, Class<? extends AvajeModule> currentModule) {
    if (entries.size() == 1) {
      return entries.get(0).provider();
    }
    return new EntryMatcher(name, currentModule).provider(entries);
  }

  /**
   * Get with strict name match for the single entry case.
   */
  Object getStrict(String name) {
    if (entries.size() == 1) {
      return entries.get(0).beanIfNameMatch(name);
    }
    return new EntryMatcher(name, null).match(entries);
  }

  Object get(String name, Class<? extends AvajeModule> currentModule) {
    if (entries.size() == 1) {
      return entries.get(0).bean();
    }
    return new EntryMatcher(name, currentModule).match(entries);
  }

  /**
   * Return all the beans.
   */
  List<Object> all() {
    List<Object> list = new ArrayList<>(entries.size());
    for (DContextEntryBean entry : entries) {
      list.add(entry.bean());
    }
    return list;
  }

  /**
   * Return a map of beans keyed by qualifier name.
   */
  Map<String, Object> map() {
    Map<String, Object> map = new LinkedHashMap<>();
    for (DContextEntryBean entry : entries) {
      Object bean = entry.bean();
      String nm = entry.name();
      if (nm == null) {
        nm = "$Unnamed-" + System.identityHashCode(bean) + "-" + bean;
      }
      map.put(nm, bean);
    }
    return map;
  }

  /**
   * Return a supplied bean is one of the entries.
   */
  DContextEntryBean supplied(String qualifierName) {
    for (DContextEntryBean entry : entries) {
      if (entry.isSupplied(qualifierName)) {
        return entry;
      }
    }
    return null;
  }

  static final class EntryMatcher {

    private final String name;
    private final boolean impliedName;
    private final Class<? extends AvajeModule> currentModule;
    private DContextEntryBean match;
    private DContextEntryBean ignoredSecondaryMatch;

    EntryMatcher(String name, Class<? extends AvajeModule> currentModule) {
      this.currentModule = currentModule;
      if (name != null && name.startsWith("!")) {
        this.name = name.substring(1);
        this.impliedName = true;
      } else {
        this.name = name;
        this.impliedName = false;
      }
    }

    private Provider<?> provider(List<DContextEntryBean> entries) {
      DContextEntryBean foundMatch = findMatch(entries);
      return foundMatch == null ? null : foundMatch.provider();
    }

    private Object match(List<DContextEntryBean> entries) {
      DContextEntryBean foundMatch = findMatch(entries);
      return foundMatch == null ? null : foundMatch.bean();
    }

    private DContextEntryBean findMatch(List<DContextEntryBean> entries) {
      for (DContextEntryBean entry : entries) {
        if (entry.isNameEqual(name)) {
          checkMatch(entry);
        }
      }
      if (match == null && impliedName) {
        // match without implied name, name = null to match against beans with no qualifier
        for (DContextEntryBean entry : entries) {
          if (entry.isNameEqual(null)) {
            checkMatch(entry);
          }
        }
      }
      if (match == null && (name == null || impliedName)) {
        // match no qualifier injection point to any beans
        for (DContextEntryBean entry : entries) {
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
      if (match.isSupplied()) {
        // existing supplied match always wins
        return;
      } else if (entry.isSupplied()) {
        // new supplied wins
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
          throw new IllegalStateException("Expecting only 1 bean match but have multiple primary beans " + match.bean() + " and " + entry.bean());
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
      // if all else fails use the one provided by the current module
      if (entry.sourceModule() != currentModule) {
        ignoredSecondaryMatch = entry;
        return;
      } else if (entry.sourceModule() == currentModule && match.sourceModule() != currentModule) {
        // match on module
        match = entry;
        return;
      }
      throw new IllegalStateException("Expecting only 1 bean match but have multiple matching beans " + match.bean()
        + " and " + entry.bean() + ". Maybe need a rebuild is required after adding a @Named qualifier?");
    }

    private DContextEntryBean candidate() {
      if (match == null) {
        return null;
      }
      checkSecondary();
      return match;
    }

    private void checkSecondary() {
      if (match.isSecondary() && ignoredSecondaryMatch != null) {
        throw new IllegalStateException("Expecting only 1 bean match but have multiple secondary beans " + match.bean() + " and " + ignoredSecondaryMatch.bean());
      }
    }

  }
}
