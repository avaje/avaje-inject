package io.avaje.inject.spi;

import io.avaje.inject.BeanScope;
import jakarta.inject.Provider;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Mutable builder object used when building a bean scope.
 */
public interface Builder {

  /**
   * Create the root level Builder.
   *
   * @param suppliedBeans  The list of beans (typically test doubles) supplied when building the context.
   * @param enrichBeans    The list of classes we want to have with mockito spy enhancement
   * @param parent         The parent BeanScope
   * @param parentOverride When false do not add beans that already exist on the parent
   */
  @SuppressWarnings("rawtypes")
  static Builder newBuilder(List<SuppliedBean> suppliedBeans, List<EnrichBean> enrichBeans, BeanScope parent, boolean parentOverride) {
    if (suppliedBeans.isEmpty() && enrichBeans.isEmpty()) {
      // simple case, no mocks or spies
      return new DBuilder(parent, parentOverride);
    }
    return new DBuilderExtn(parent, parentOverride, suppliedBeans, enrichBeans);
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
  boolean isAddBeanFor(String name, Type... types);

  /**
   * Return true if the bean should be created and registered with the context.
   * <p/>
   * Returning false means there has been a supplied bean already registered and
   * that we should skip the creation and registration for this bean.
   *
   * @param types The types that the bean implements and provides
   */
  boolean isAddBeanFor(Type... types);

  /**
   * Register the provider into the context.
   */
  <T> void registerProvider(Provider<T> provider);

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
   * Register the externally provided bean.
   *
   * @param type The type of the provided bean.
   * @param bean The bean instance
   */
  <T> void withBean(Class<T> type, T bean);

  /**
   * Add lifecycle PostConstruct method.
   */
  void addPostConstruct(Runnable runnable);

  /**
   * Add lifecycle PreDestroy method.
   */
  void addPreDestroy(AutoCloseable closeable);

  /**
   * Add field and method injection.
   */
  void addInjector(Consumer<Builder> injector);

  /**
   * Get an optional dependency.
   */
  <T> Optional<T> getOptional(Type cls);

  /**
   * Get an optional named dependency.
   */
  <T> Optional<T> getOptional(Type cls, String name);

  /**
   * Get an optional dependency potentially returning null.
   */
  <T> T getNullable(Type cls);

  /**
   * Get an optional named dependency potentially returning null.
   */
  <T> T getNullable(Type cls, String name);

  /**
   * Return Provider of T given the type.
   */
  <T> Provider<T> getProvider(Type cls);

  /**
   * Return Provider of T given the type and name.
   */
  <T> Provider<T> getProvider(Type cls, String name);

  /**
   * Return Provider for a generic interface type.
   *
   * @param cls  The usual implementation class
   * @param type The generic interface type
   */
  <T> Provider<T> getProviderFor(Class<?> cls, Type type);

  /**
   * Get a dependency.
   */
  <T> T get(Type cls);

  /**
   * Get a named dependency.
   */
  <T> T get(Type cls, String name);

  /**
   * Get a list of dependencies for the type.
   */
  <T> List<T> list(Type interfaceType);

  /**
   * Get a set of dependencies for the type.
   */
  <T> Set<T> set(Type interfaceType);

  /**
   * Return a map of dependencies keyed by qualifier name.
   */
  <T> Map<String, T> map(Type interfaceType);

  /**
   * Build and return the bean scope.
   */
  BeanScope build(boolean withShutdownHook);
}
