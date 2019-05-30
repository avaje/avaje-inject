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
   * Register beans with potential enhancement (spy).
   */
  @Override
  public void register(Object bean, String name, Class<?>... types) {
    if (parent != null) {
      throw new IllegalStateException("no");
    }
    // this is the top level builder (parent always null)
    bean = enrich(bean, types);
    beanMap.register(bean, name, types);
  }

  /**
   * Potentially enrich the bean prior to registering with context.
   */
  @SuppressWarnings("unchecked")
  @Override
  public Object enrich(Object bean, Class<?>[] types) {
    EnrichBean enrich = enrichMap.get(typeOf(bean, types));
    return enrich != null ? enrich.enrich(bean) : bean;
  }

  /**
   * Return the type to lookup for enrichment.
   */
  private Class<?> typeOf(Object bean, Class<?>... types) {
    if (types.length > 0) {
      return types[0];
    }
    return bean.getClass();
  }
}
