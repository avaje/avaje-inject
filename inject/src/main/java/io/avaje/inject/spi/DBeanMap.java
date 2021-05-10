package io.avaje.inject.spi;

import io.avaje.inject.BeanEntry;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static io.avaje.inject.BeanEntry.*;

/**
 * Map of types (class types, interfaces and annotations) to a DContextEntry where the
 * entry holds a list of bean instances for that type.
 */
class DBeanMap {

  private final Map<String, DContextEntry> beans = new LinkedHashMap<>();

  /**
   * Create for context builder.
   */
  DBeanMap() {
  }

  /**
   * Add test double supplied beans.
   */
  @SuppressWarnings("rawtypes")
  void add(List<SuppliedBean> suppliedBeans) {
    for (SuppliedBean suppliedBean : suppliedBeans) {
      addSuppliedBean(suppliedBean);
    }
  }

  @SuppressWarnings("rawtypes")
  private void addSuppliedBean(SuppliedBean supplied) {
    Class<?> suppliedType = supplied.getType();
    DContextEntryBean entryBean = DContextEntryBean.of(supplied.getBean(), supplied.name(), SUPPLIED);
    beans.computeIfAbsent(suppliedType.getCanonicalName(), s -> new DContextEntry()).add(entryBean);
    for (Class<?> anInterface : suppliedType.getInterfaces()) {
      beans.computeIfAbsent(anInterface.getCanonicalName(), s -> new DContextEntry()).add(entryBean);
    }
  }

  void registerPrimary(Object bean, String name, Class<?>... types) {
    registerWith(PRIMARY, bean, name, types);
  }

  void registerSecondary(Object bean, String name, Class<?>... types) {
    registerWith(SECONDARY, bean, name, types);
  }

  void register(Object bean, String name, Class<?>... types) {
    registerWith(NORMAL, bean, name, types);
  }

  void registerWith(int flag, Object bean, String name, Class<?>... types) {
    DContextEntryBean entryBean = DContextEntryBean.of(bean, name, flag);
    for (Class<?> type : types) {
      beans.computeIfAbsent(type.getCanonicalName(), s -> new DContextEntry()).add(entryBean);
    }
  }

  <T> BeanEntry<T> candidate(Class<T> type, String name) {
    DContextEntry entry = beans.get(type.getCanonicalName());
    if (entry != null) {
      if (name != null) {
        name = name.toLowerCase();
      }
      return entry.candidate(name);
    }
    return null;
  }

  /**
   * Add all bean instances matching the given type to the list.
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  void addAll(Class type, List list) {
    DContextEntry entry = beans.get(type.getCanonicalName());
    if (entry != null) {
      entry.addAll(list);
    }
  }

  /**
   * Return true if there is a supplied bean for the name and types.
   */
  boolean isSupplied(String qualifierName, Class<?>... types) {
    if (types != null) {
      for (Class<?> type : types) {
        if (isSuppliedType(qualifierName, type)) {
          return true;
        }
      }
    }
    return false;
  }

  private boolean isSuppliedType(String qualifierName, Class<?> type) {
    DContextEntry entry = beans.get(type.getCanonicalName());
    return entry != null && entry.isSupplied(qualifierName);
  }
}
