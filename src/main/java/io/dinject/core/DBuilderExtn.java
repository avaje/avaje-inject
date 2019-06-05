package io.dinject.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Extended builder that supports supplied beans (mocks) and enriching beans (spy).
 */
public class DBuilderExtn extends DBuilder {

  private final Map<Class<?>, EnrichBean> enrichMap = new HashMap<>();

  private final boolean hasSuppliedBeans;

  DBuilderExtn(List<SuppliedBean> suppliedBeans, List<EnrichBean> enrichBeans) {
    super();
    this.hasSuppliedBeans = (suppliedBeans != null && !suppliedBeans.isEmpty());
    if (hasSuppliedBeans) {
      beanMap.add(suppliedBeans);
    }
    if (enrichBeans != null && !enrichBeans.isEmpty()) {
      for (EnrichBean spy : enrichBeans) {
        enrichMap.put(spy.getType(), spy);
      }
    }
  }

  /**
   * Checking for supplied beans (mocks).
   */
  @Override
  public boolean isAddBeanFor(Class<?> addForType, Class<?> injectTarget) {
    if (hasSuppliedBeans) {
      return !beanMap.isSupplied(addForType.getName());
    }
    return true;
  }

  /**
   * Potentially enrich the bean prior to registering with context.
   */
  @SuppressWarnings("unchecked")
  @Override
  public Object enrich(Object bean, Class<?>[] types) {
    EnrichBean enrich = getEnrich(bean, types);
    return enrich != null ? enrich.enrich(bean) : bean;
  }

  /**
   * Search for EnrichBean on the bean or any of the interface types.
   */
  private EnrichBean getEnrich(Object bean, Class<?>[] types) {

    EnrichBean enrich = enrichMap.get(bean.getClass());
    if (enrich != null) {
      return enrich;
    }
    for (Class<?> type : types) {
      enrich = enrichMap.get(type);
      if (enrich != null) {
        return enrich;
      }
    }
    return null;
  }

}
