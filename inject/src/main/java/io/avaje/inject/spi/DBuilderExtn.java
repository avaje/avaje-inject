package io.avaje.inject.spi;

import io.avaje.inject.BeanScope;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Extended builder that supports supplied beans (mocks) and enriching beans (spy).
 */
class DBuilderExtn extends DBuilder {

  @SuppressWarnings("rawtypes")
  private final Map<String, EnrichBean> enrichMap = new HashMap<>();

  private final boolean hasSuppliedBeans;

  @SuppressWarnings("rawtypes")
  DBuilderExtn(BeanScope parent, List<SuppliedBean> suppliedBeans, List<EnrichBean> enrichBeans) {
    super(parent);
    this.hasSuppliedBeans = (suppliedBeans != null && !suppliedBeans.isEmpty());
    if (hasSuppliedBeans) {
      beanMap.add(suppliedBeans);
    }
    if (enrichBeans != null && !enrichBeans.isEmpty()) {
      for (EnrichBean spy : enrichBeans) {
        enrichMap.put(spy.key(), spy);
      }
    }
  }

  @Override
  public boolean isAddBeanFor(String qualifierName, Type... types) {
    next(qualifierName, types);
    if (hasSuppliedBeans) {
      return !beanMap.isSupplied(qualifierName, types);
    }
    return true;
  }

  /**
   * Potentially enrich the bean prior to registering with context.
   */
  @Override
  protected  <T> T enrich(T bean, DBeanMap.NextBean next) {
    EnrichBean<T> enrich = enrichLookup(bean.getClass(), next.name);
    if (enrich != null) {
      return enrich.enrich(bean);
    }
    for (Type type : next.types) {
      enrich = enrichLookup(type, next.name);
      if (enrich != null) {
        return enrich.enrich(bean);
      }
    }
    // no enrichment for this bean
    return bean;
  }

  @SuppressWarnings({"unchecked"})
  private <T> EnrichBean<T> enrichLookup(Type type, String name) {
    EnrichBean<T> enrich = enrichMap.get(KeyUtil.key(type, null));
    if (enrich == null && name != null) {
      enrich = enrichMap.get(KeyUtil.key(type, name));
    }
    return enrich;
  }

}
