package io.avaje.inject.spi;

import io.avaje.inject.BeanEntry;

import jakarta.inject.Named;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static io.avaje.inject.BeanEntry.NORMAL;
import static io.avaje.inject.BeanEntry.PRIMARY;
import static io.avaje.inject.BeanEntry.SECONDARY;
import static io.avaje.inject.BeanEntry.SUPPLIED;

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
    Named annotation = suppliedType.getAnnotation(Named.class);
    String name = (annotation == null) ? null : annotation.value();

    DContextEntryBean entryBean = DContextEntryBean.of(supplied.getBean(), name, SUPPLIED);
    beans.computeIfAbsent(suppliedType.getCanonicalName(), s -> new DContextEntry()).add(entryBean);
    for (Class<?> anInterface : suppliedType.getInterfaces()) {
      beans.computeIfAbsent(anInterface.getCanonicalName(), s -> new DContextEntry()).add(entryBean);
    }
  }

  void registerPrimary(String canonicalName, Object bean, String name, Class<?>... types) {
    registerWith(PRIMARY, canonicalName, bean, name, types);
  }

  void registerSecondary(String canonicalName, Object bean, String name, Class<?>... types) {
    registerWith(SECONDARY, canonicalName, bean, name, types);
  }

  void register(String canonicalName, Object bean, String name, Class<?>... types) {
    registerWith(NORMAL, canonicalName, bean, name, types);
  }

  void registerWith(int flag, String canonicalName, Object bean, String name, Class<?>... types) {
    DContextEntryBean entryBean = DContextEntryBean.of(bean, name, flag);
    beans.computeIfAbsent(canonicalName, s -> new DContextEntry()).add(entryBean);
    if (types != null) {
      for (Class<?> type : types) {
        beans.computeIfAbsent(type.getCanonicalName(), s -> new DContextEntry()).add(entryBean);
      }
    }
  }

  <T> BeanEntry<T> candidate(Class<T> type, String name) {
    DContextEntry entry = beans.get(type.getCanonicalName());
    if (entry != null) {
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
   * Return true if there is a supplied bean for this type.
   */
  boolean isSupplied(String canonicalName) {
    DContextEntry entry = beans.get(canonicalName);
    return entry != null && entry.isSupplied();
  }
}
