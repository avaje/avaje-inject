package io.avaje.inject.spi;

import io.avaje.inject.BeanContext;
import io.avaje.inject.BeanEntry;
import jakarta.inject.Provider;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Mutable builder object used when building a bean context.
 */
public interface Builder {

  /**
   * Create the root level Builder.
   *
   * @param suppliedBeans The list of beans (typically test doubles) supplied when building the context.
   * @param enrichBeans   The list of classes we want to have with mockito spy enhancement
   */
  @SuppressWarnings("rawtypes")
  static Builder newBuilder(List<SuppliedBean> suppliedBeans, List<EnrichBean> enrichBeans) {
    if (suppliedBeans.isEmpty() && enrichBeans.isEmpty()) {
      // simple case, no mocks or spies
      return new DBuilder();
    }
    return new DBuilderExtn(suppliedBeans, enrichBeans);
  }

  /**
   * Return true if the bean should be created and registered with the context.
   * <p/>
   * Returning false means there has been a supplied bean already registered and
   * that we should skip the creation and registration for this bean.
   *
   * @param name  The qualifier name
   * @param types The types that the bean implements and provides
   */
  boolean isAddBeanFor(String name, Class<?>... types);

  /**
   * Return true if the bean should be created and registered with the context.
   * <p/>
   * Returning false means there has been a supplied bean already registered and
   * that we should skip the creation and registration for this bean.
   *
   * @param types The types that the bean implements and provides
   */
  boolean isAddBeanFor(Class<?>... types);

  /**
   * Register the bean instance into the context.
   *
   * @param bean The bean instance that has been created.
   */
  <T> T register(T bean);

  /**
   * Register the bean as a Primary bean.
   */
  <T> T registerPrimary(T bean);

  /**
   * Register the bean as a secondary bean.
   */
  <T> T registerSecondary(T bean);

  /**
   * Add a lifecycle bean.
   */
  void addLifecycle(BeanLifecycle bean);

  /**
   * Add field and method injection.
   */
  void addInjector(Consumer<Builder> injector);

  /**
   * Get an optional dependency.
   */
  <T> Optional<T> getOptional(Class<T> cls);

  /**
   * Get an optional named dependency.
   */
  <T> Optional<T> getOptional(Class<T> cls, String name);

  /**
   * Get an optional dependency potentially returning null.
   */
  <T> T getNullable(Class<T> cls);

  /**
   * Get an optional named dependency potentially returning null.
   */
  <T> T getNullable(Class<T> cls, String name);

  /**
   * Return Provider of T given the type.
   */
  <T> Provider<T> getProvider(Class<T> cls);

  /**
   * Return Provider of T given the type and name.
   */
  <T> Provider<T> getProvider(Class<T> cls, String name);

  /**
   * Get a dependency.
   */
  <T> T get(Class<T> cls);

  /**
   * Get a named dependency.
   */
  <T> T get(Class<T> cls, String name);

  /**
   * Get a list of dependencies for the interface type .
   */
  <T> List<T> getList(Class<T> interfaceType);

  /**
   * Get a set of dependencies for the interface type .
   */
  <T> Set<T> getSet(Class<T> interfaceType);

  /**
   * Get a candidate dependency allowing it to be null.
   */
  <T> BeanEntry<T> candidate(Class<T> cls, String name);

  /**
   * Return a potentially enriched bean for registration into the context.
   * Typically for use with mockito spy.
   *
   * @param bean  The bean with dependencies injected
   * @param types The types this bean registers for
   * @return Either the bean or the enriched bean to register into the context.
   */
  <T> T enrich(T bean, Class<?>[] types);

  /**
   * Build and return the bean context.
   */
  BeanContext build();
}
