package io.avaje.inject.spi;

import io.avaje.inject.BeanScope;
import io.avaje.inject.BeanEntry;
import io.avaje.inject.RequestScopeMatch;
import io.avaje.inject.RequestScopeProvider;
import javax.inject.Provider;

import java.util.*;
import java.util.function.Consumer;

class DBuilder implements Builder {

  /**
   * List of Lifecycle beans.
   */
  private final List<BeanLifecycle> lifecycleList = new ArrayList<>();

  /**
   * List of field injection closures.
   */
  private final List<Consumer<Builder>> injectors = new ArrayList<>();

  /**
   * Map of request scope providers and their associated keys.
   */
  private final Map<String, RequestScopeMatch<?>> reqScopeProviders = new HashMap<>();

  /**
   * The beans created and added to the context during building.
   */
  final DBeanMap beanMap = new DBeanMap();

  /**
   * Debug of the current bean being wired - used in injection errors.
   */
  private Class<?> injectTarget;

  /**
   * Flag set when we are running post construct injection.
   */
  private boolean runningPostConstruct;

  @Override
  public boolean isAddBeanFor(Class<?>... types) {
    return isAddBeanFor(null, types);
  }

  @Override
  public boolean isAddBeanFor(String name, Class<?>... types) {
    next(name, types);
    return true;
  }

  protected void next(String name, Class<?>... types) {
    injectTarget = firstOf(types);
    beanMap.nextBean(name, types);
  }

  @Override
  public <T> void requestScope(Class<T> type, RequestScopeProvider<T> provider) {
    requestScope(type, provider, null, null);
  }

  @Override
  public <T> void requestScope(Class<T> type, RequestScopeProvider<T> provider, String name, Class<?>... types) {
    final DRequestScopeMatch<T> match = DRequestScopeMatch.of(provider, type, name, types);
    for (String key : match.keys()) {
      reqScopeProviders.put(key, match);
    }
  }

  private Class<?> firstOf(Class<?>[] types) {
    return types != null && types.length > 0 ? types[0] : null;
  }

  @Override
  public <T> Set<T> getSet(Class<T> interfaceType) {
    return new LinkedHashSet<>(getList(interfaceType));
  }

  @SuppressWarnings({"unchecked"})
  @Override
  public <T> List<T> getList(Class<T> interfaceType) {
    return (List<T>) beanMap.all(interfaceType);
  }

  private <T> T getMaybe(Class<T> beanClass, String name) {
    return beanMap.get(beanClass, name);
  }

  /**
   * Return the bean to register potentially with spy enhancement.
   */
  @Override
  public <T> T enrich(T bean, Class<?>[] types) {
    // only enriched by DBuilderExtn
    return bean;
  }

  @Override
  public <T> T register(T bean) {
    return register(BeanEntry.NORMAL, bean);
  }

  @Override
  public <T> T registerPrimary(T bean) {
    return register(BeanEntry.PRIMARY, bean);
  }

  @Override
  public <T> T registerSecondary(T bean) {
    return register(BeanEntry.SECONDARY, bean);
  }

  private <T> T register(int flag, T bean) {
    bean = enrich(bean, beanMap.types());
    beanMap.register(flag, bean);
    return bean;
  }

  @Override
  public void addLifecycle(BeanLifecycle wrapper) {
    lifecycleList.add(wrapper);
  }

  @Override
  public void addInjector(Consumer<Builder> injector) {
    injectors.add(injector);
  }

  @Override
  public <T> Optional<T> getOptional(Class<T> cls) {
    return getOptional(cls, null);
  }

  @Override
  public <T> Optional<T> getOptional(Class<T> cls, String name) {
    T bean = getMaybe(cls, name);
    return Optional.ofNullable(bean);
  }

  @Override
  public <T> T getNullable(Class<T> cls) {
    return getNullable(cls, null);
  }

  @Override
  public <T> T getNullable(Class<T> cls, String name) {
    return getMaybe(cls, name);
  }

  @Override
  public <T> Provider<T> getProvider(Class<T> cls) {
    return getProvider(cls, null);
  }

  @Override
  public <T> Provider<T> getProvider(Class<T> cls, String name) {
    if (runningPostConstruct) {
      return new ProviderWrapper<>(get(cls, name));
    }
    ProviderPromise<T> promise = new ProviderPromise<>(cls, name);
    injectors.add(promise);
    return promise;
  }

  @Override
  public <T> T get(Class<T> cls) {
    return get(cls, null);
  }

  @Override
  public <T> T get(Class<T> cls, String name) {
    T bean = getMaybe(cls, name);
    if (bean == null) {
      String msg = "Injecting null for " + cls.getName();
      if (name != null) {
        msg += " name:" + name;
      }
      List<T> beanList = getList(cls);
      msg += " when creating " + injectTarget + " - potential beans to inject: " + beanList;
      if (!beanList.isEmpty()) {
        msg += ". Check @Named or Qualifier being used";
      }
      throw new IllegalStateException(msg);
    }
    return bean;
  }

  private void runInjectors() {
    runningPostConstruct = true;
    for (Consumer<Builder> injector : injectors) {
      injector.accept(this);
    }
  }

  public BeanScope build(boolean withShutdownHook) {
    runInjectors();
    return new DBeanScope(withShutdownHook, lifecycleList, beanMap, reqScopeProviders).start();
  }
}
