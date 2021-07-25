package io.avaje.inject.spi;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static io.avaje.inject.BeanEntry.SUPPLIED;

/**
 * Map of types (class types, interfaces and annotations) to a DContextEntry where the
 * entry holds a list of bean instances for that type.
 */
class DBeanMap {

  private final Map<String, DContextEntry> beans = new LinkedHashMap<>();

  private NextBean nextBean;

  DBeanMap() {
  }

  /**
   * Add to the map of entries.
   */
  void addAll(Map<DContextEntryBean, DEntry> map) {
    for (Map.Entry<String, DContextEntry> entry : beans.entrySet()) {
      for (DContextEntryBean contentEntry : entry.getValue().entries()) {
        map.computeIfAbsent(contentEntry, dContextEntryBean -> contentEntry.entry()).addKey(entry.getKey());
      }
    }
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
    beans.computeIfAbsent(suppliedType.getTypeName(), s -> new DContextEntry()).add(entryBean);
    for (Class<?> anInterface : suppliedType.getInterfaces()) {
      beans.computeIfAbsent(anInterface.getTypeName(), s -> new DContextEntry()).add(entryBean);
    }
  }

  void register(int flag, Object bean) {
    DContextEntryBean entryBean = DContextEntryBean.of(bean, nextBean.name, flag);
    for (Type type : nextBean.types) {
      beans.computeIfAbsent(type.getTypeName(), s -> new DContextEntry()).add(entryBean);
    }
  }

  @SuppressWarnings("unchecked")
  <T> T get(Class<T> type, String name) {
    DContextEntry entry = beans.get(type.getTypeName());
    if (entry == null) {
      return null;
    }
    return (T) entry.get(KeyUtil.lower(name));
  }

  /**
   * Return all bean instances matching the given type.
   */
  @SuppressWarnings("rawtypes")
  List<Object> all(Class type) {
    DContextEntry entry = beans.get(type.getTypeName());
    return entry != null ? entry.all() : Collections.emptyList();
  }

  /**
   * Return true if there is a supplied bean for the name and types.
   */
  boolean isSupplied(String qualifierName, Type... types) {
    if (types != null) {
      for (Type type : types) {
        if (isSuppliedType(qualifierName, type)) {
          return true;
        }
      }
    }
    return false;
  }

  private boolean isSuppliedType(String qualifierName, Type type) {
    DContextEntry entry = beans.get(type.getTypeName());
    return entry != null && entry.isSupplied(qualifierName);
  }

  /**
   * Store the qualifier name and type for the next bean to register.
   */
  void nextBean(String name, Type[] types) {
    nextBean = new NextBean(name, types);
  }

  /**
   * Return the types of the bean being processed/registered.
   */
  NextBean next() {
    return nextBean;
  }

  static class NextBean {
    final String name;
    final Type[] types;

    NextBean(String name, Type[] types) {
      this.name = name;
      this.types = types;
    }
  }
}
