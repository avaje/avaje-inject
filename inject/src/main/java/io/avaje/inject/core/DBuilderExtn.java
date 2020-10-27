package io.avaje.inject.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Extended builder that supports supplied beans (mocks) and enriching beans (spy).
 */
class DBuilderExtn extends DBuilder {

  @SuppressWarnings("rawtypes")
  private final Map<Class<?>, EnrichBean> enrichMap = new HashMap<>();

  private final boolean hasSuppliedBeans;

  @SuppressWarnings("rawtypes")
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
      return !beanMap.isSupplied(addForType.getCanonicalName());
    }
    return true;
  }

  /**
   * Potentially enrich the bean prior to registering with context.
   */
  @SuppressWarnings("unchecked")
  @Override
  public <T> T enrich(T bean, Class<?>[] types) {
    EnrichBean<T> enrich = getEnrich(bean, types);
    return enrich != null ? enrich.enrich(bean) : bean;
  }

  /**
   * Search for EnrichBean on the bean or any of the interface types.
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  private <T> EnrichBean getEnrich(T bean, Class<?>[] types) {

    EnrichBean<T> enrich = enrichMap.get(bean.getClass());
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
