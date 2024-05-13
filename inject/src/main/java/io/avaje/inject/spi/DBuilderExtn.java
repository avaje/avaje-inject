package io.avaje.inject.spi;

import io.avaje.inject.BeanEntry;
import io.avaje.inject.BeanScope;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Extended builder that supports supplied beans (mocks) and enriching beans (spy).
 */
final class DBuilderExtn extends DBuilder {

  @SuppressWarnings("rawtypes")
  private final Map<String, EnrichBean> enrichMap = new HashMap<>();

  private final boolean hasSuppliedBeans;

  @SuppressWarnings("rawtypes")
  DBuilderExtn(Set<String> profiles, PropertyPlugin plugin, BeanScope parent, boolean parentOverride, List<SuppliedBean> suppliedBeans, List<EnrichBean> enrichBeans) {
    super(profiles, plugin, parent, parentOverride);
    this.hasSuppliedBeans = (suppliedBeans != null && !suppliedBeans.isEmpty());
    if (hasSuppliedBeans) {
      beanMap.add(suppliedBeans);
    }
    if (enrichBeans != null && !enrichBeans.isEmpty()) {
      for (final EnrichBean spy : enrichBeans) {
        enrichMap.put(spy.key(), spy);
      }
    }
  }

  @Override
  public boolean isAddBeanFor(String qualifierName, Type... types) {
    if (!super.isAddBeanFor(qualifierName, types)) {
      enrichParentMatch();
      return false;
    }
    if (hasSuppliedBeans) {
      return !beanMap.isSupplied(qualifierName, removeAnnotations(types));
    }
    return true;
  }

  /**
   * If we have a parentMatch (e.g. test scope bean) but we want to enrich it (Mockito Spy),
   * then enrich the parentMatch bean and register that into this scope.
   */
  private void enrichParentMatch() {
    if (parentMatch != null && !enrichMap.isEmpty()) {
      final Object enrichedBean = enrich(parentMatch, beanMap.next());
      if (enrichedBean != parentMatch) {
        beanMap.nextPriority(BeanEntry.SUPPLIED);
        beanMap.register(enrichedBean);
      }
    }
  }

  /**
   * Potentially enrich the bean prior to registering with context.
   */
  @Override
  protected <T> T enrich(T bean, DBeanMap.NextBean next) {
    EnrichBean<T> enrich = enrichLookup(bean.getClass(), next.name);
    if (enrich != null) {
      return enrich.enrich(bean);
    }
    for (final Type type : next.types) {
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
