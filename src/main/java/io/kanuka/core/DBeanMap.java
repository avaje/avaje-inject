package io.kanuka.core;

import javax.inject.Named;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Map of types (class types, interfaces and annotations) to a DContextEntry where the
 * entry holds a list of bean instances for that type.
 */
class DBeanMap {

  private final Map<String, DContextEntry> beans;

  /**
   * Create for root builder with supplied beans (usually test doubles).
   * <p>
   * Supplied beans are typically test doubles (used in testing) that replace the normally
   * injected bean. For example, a stub for a database API.
   * </p>
   */
  DBeanMap(List<Object> suppliedBeans) {
    if (suppliedBeans.isEmpty()) {
      beans = null;
    } else {
      beans = new LinkedHashMap<>();
      for (Object suppliedBean : suppliedBeans) {
        addSuppliedBean(suppliedBean);
      }
    }
  }

  /**
   * Create for context builder.
   */
  DBeanMap() {
    beans = new LinkedHashMap<>();
  }

  private void addSuppliedBean(Object bean) {

    Class<?> suppliedClass = bean.getClass();
    Class<?> suppliedType = suppliedType(suppliedClass);
    Named annotation = suppliedClass.getAnnotation(Named.class);
    String name = (annotation == null) ? null : annotation.value();

    DContextEntryBean entryBean = new DContextEntryBean(bean, name);
    beans.computeIfAbsent(suppliedType.getCanonicalName(), s -> new DContextEntry()).add(entryBean);
    for (Class<?> anInterface : suppliedClass.getInterfaces()) {
      beans.computeIfAbsent(anInterface.getCanonicalName(), s -> new DContextEntry()).add(entryBean);
    }
  }

  /**
   * Return the type that we map the supplied bean to.
   */
  private Class<?> suppliedType(Class<?> suppliedClass) {
    Class<?> suppliedSuper = suppliedClass.getSuperclass();
    if (Object.class.equals(suppliedSuper)) {
      return suppliedClass;
    } else {
      // prefer to use the super type of the supplied bean (test double)
      return suppliedSuper;
    }
  }


  /**
   * Add bean during normal context building.
   *
   * @param bean           The bean that has been created.
   * @param name           The optional name of the bean that has just been created (via @Named).
   * @param interfaceClass The interfaces and annotations we associate the bean instance with.
   */
  void addBean(Object bean, String name, String... interfaceClass) {

    DContextEntryBean entryBean = new DContextEntryBean(bean, name);
    beans.computeIfAbsent(bean.getClass().getCanonicalName(), s -> new DContextEntry()).add(entryBean);

    if (interfaceClass != null) {
      for (String aClass : interfaceClass) {
        beans.computeIfAbsent(aClass, s -> new DContextEntry()).add(entryBean);
      }
    }
  }

  /**
   * Return the bean instance given the class and name.
   */
  @SuppressWarnings("unchecked")
  <T> T getBean(Class<T> type, String name) {

    if (beans == null) {
      // no beans in the root suppliedBeanMap
      return null;
    }
    DContextEntry entry = beans.get(type.getCanonicalName());
    if (entry != null) {
      T bean = (T) entry.get(name);
      if (bean != null) {
        return bean;
      }
    }
    return null;
  }

  /**
   * Add all bean instances matching the given type to the list.
   */
  void addAll(Class type, List list) {
    if (beans != null) {
      DContextEntry entry = beans.get(type.getCanonicalName());
      if (entry != null) {
        entry.addAll(list);
      }
    }
  }

  /**
   * Return true if the bean for the given type should be created.
   * <p>
   * Return false indicates the type has a supplied (test double) instance that should be used instead and that
   * means context building will skip creating this bean.
   * </p>
   */
  boolean isAddBeanFor(String type) {
    if (beans == null) {
      return true;
    }
    return !beans.containsKey(type);
  }
}
